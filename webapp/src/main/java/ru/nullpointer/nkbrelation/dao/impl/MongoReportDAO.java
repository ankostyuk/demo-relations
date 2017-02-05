package ru.nullpointer.nkbrelation.dao.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.data.domain.Sort;
import static org.springframework.data.domain.Sort.Direction.*;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import static org.springframework.data.mongodb.core.query.Query.query;
import org.springframework.util.Assert;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;

import ru.nullpointer.nkbrelation.dao.DaoException;
import ru.nullpointer.nkbrelation.dao.ReportDAO;
import ru.nullpointer.nkbrelation.domain.report.Report;

/**
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public class MongoReportDAO implements ReportDAO, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MongoReportDAO.class); // NOPMD
    //

    private static final String USER_IMAGE_FILE_NAME_PREFIX = "user_object_image_";
    private static final String USER_IMAGE_FILE_ENCODING = "UTF-8";
    //
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private GridFsTemplate mongoGridFsTemplate;

    @Override
    public Report getById(String id) {
        Assert.hasText(id);

        Report report = mongoTemplate.findById(id, Report.class);
        fillReportImages(report);

        return report;
    }

    @Override
    public Report getByShareKey(String key) {
        Assert.hasText(key);

        Report report = mongoTemplate.findOne(query(where("shareKey").is(key)), Report.class);
        fillReportImages(report);

        return report;
    }

    @Override
    public void insert(Report report) {
        Assert.notNull(report);
        report.setId(null);

        storeReportImages(report, null);
        mongoTemplate.insert(report);
    }

    @Override
    public void update(Report report) {
        Assert.notNull(report);
        Assert.hasText(report.getId());

        storeReportImages(report, report.getId());
        mongoTemplate.save(report);
    }

    @Override
    public void delete(String id) {
        Assert.hasText(id);

        Report report = mongoTemplate.findAndRemove(query(where("id").is(id)), Report.class);

        Assert.notNull(report);
        deleteReportImages(report);
    }

    @Override
    public List<Report> getListByUserId(String userId) {
        Assert.hasText(userId);

        Query q = query(where("userId").is(userId));
        q.with(new Sort(DESC, "edited"));
        q.fields().exclude("style");
        q.fields().exclude("nodes");
        q.fields().exclude("relations");
        q.fields().exclude("links");
        q.fields().exclude("userObjects");

        return mongoTemplate.find(q, Report.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getReportUserObjects(Report report) {
        List<Map<String, Object>> userObjects = report.getUserObjects();
        return CollectionUtils.isNotEmpty(userObjects) ? userObjects : Collections.EMPTY_LIST;
    }

    /**
     * Сохранение изображений отчета {@code report} в отдельное хранилище.
     * Сами изображения {@code image} будут удалены из объекта {@code userObject}.
     * В объект {@code userObject} будет добавлена метаинформация {@code imageMeta} о каждом изображении.
     * При отдаче отчета изображения будут добавлены в объект {@code userObject} из хранилища,
     * см. {@link #fillReportImages(Report report) fillReportImages}
     */
    @SuppressWarnings("unchecked")
    private void storeReportImages(Report report, String storedReportId) {
        Map<String, Object> storedImageMap = new HashMap<String, Object>();
        Set<String> userObjectIds = new HashSet<String>();
        Set<String> deletedImageIds = new HashSet<String>();

        if (StringUtils.isNotBlank(storedReportId)) {
            Report storedReport = mongoTemplate.findById(storedReportId, Report.class);

            for (Map<String, Object> userObject : getReportUserObjects(storedReport)) {
                String userObjectId = MapUtils.getString(userObject, "_id");
                Map<String, Object> imageMeta = MapUtils.getMap(userObject, "imageMeta");

                if (isFileMetaValid(imageMeta)) {
                    storedImageMap.put(userObjectId, imageMeta);
                }
            }
        }

        try {
            for (Map<String, Object> userObject : getReportUserObjects(report)) {
                String userObjectId = MapUtils.getString(userObject, "_id");
                String image = MapUtils.getString(userObject, "image");
                Map<String, Object> storedImageMeta = MapUtils.getMap(storedImageMap, userObjectId);

                userObject.remove("image");

                userObjectIds.add(userObjectId);

                if (StringUtils.isBlank(image)) {
                    if (MapUtils.isNotEmpty(storedImageMeta)) {
                        // изображение удалено пользователем
                        deletedImageIds.add(MapUtils.getString(storedImageMeta, "id"));
                    }
                    continue;
                }

                Map<String, Object> imageMeta = null;
                String imageHash = DigestUtils.md5Hex(image);
                String imageFileName = USER_IMAGE_FILE_NAME_PREFIX + userObjectId;
                boolean touch = false;

                if (MapUtils.isNotEmpty(storedImageMeta)) {
                    if (!imageHash.equals(MapUtils.getString(storedImageMeta, "hash"))) {
                        // изображение изменено пользователем
                        touch = true;
                        deletedImageIds.add(MapUtils.getString(storedImageMeta, "id"));
                    }
                } else {
                    // изображение добавлено пользователем
                    touch = true;
                }

                if (touch) {
                    InputStream imageIS = IOUtils.toInputStream(image, USER_IMAGE_FILE_ENCODING);
                    imageMeta = storeFile(imageIS, imageFileName, imageHash);
                    userObject.put("imageMeta", imageMeta);
                } else {
                    userObject.put("imageMeta", MapUtils.isNotEmpty(storedImageMeta) ? storedImageMeta : null);
                }
            }
        } catch (IOException e) {
            throw new DaoException("Could not store images in report", e);
        }

        for (String userObjectId : storedImageMap.keySet()) {
            if (!userObjectIds.contains(userObjectId)) {
                // объект с изображением удален пользователем
                Map<String, Object> storedImageMeta = MapUtils.getMap(storedImageMap, userObjectId);
                deletedImageIds.add(MapUtils.getString(storedImageMeta, "id"));
            }
        }

        if (CollectionUtils.isNotEmpty(deletedImageIds)) {
            deleteFiles(deletedImageIds);
        }
    }

    /**
     * Заполнение изображений в отчет {@code report} из хранилища.
     * В объект {@code userObject} будут добавлены данные {@code image} каждого изображения.
     * Метаинформация {@code imageMeta} каждого изображения будет удалена из объекта {@code userObject}.
     */
    @SuppressWarnings("unchecked")
    private void fillReportImages(Report report) {
        Map<String, GridFSDBFile> imageFileMap = getReportImageFileMap(report);

        try {
            for (Map<String, Object> userObject : getReportUserObjects(report)) {
                Map<String, Object> imageMeta = MapUtils.getMap(userObject, "imageMeta");

                userObject.remove("imageMeta");

                if (MapUtils.isEmpty(imageMeta)) {
                    continue;
                }

                String imageId = MapUtils.getString(imageMeta, "id");
                GridFSDBFile imageFile = imageFileMap.get(imageId);

                if (imageFile == null) {
                    logger.warn("fillReportImages... imageFile is null,  imageMeta: {}, userObjectId: {}, reportId: {}",
                            imageMeta, MapUtils.getString(userObject, "_id"), report.getId());

                    continue;
                }

                InputStream imageIS = imageFile.getInputStream();
                String image = IOUtils.toString(imageIS, USER_IMAGE_FILE_ENCODING);

                userObject.put("image", image);
            }
        } catch (IOException e) {
            throw new DaoException("Could not fill images in report", e);
        }
    }

    private void deleteReportImages(Report report) {
        Set<String> imageIds = getReportImageMap(report).keySet();
        if (CollectionUtils.isNotEmpty(imageIds)) {
            deleteFiles(imageIds);
        }
    }

    private boolean isFileMetaValid(Map<String, Object> fileMeta) {
        return MapUtils.isNotEmpty(fileMeta) &&
                StringUtils.isNotBlank(MapUtils.getString(fileMeta, "id")) &&
                StringUtils.isNotBlank(MapUtils.getString(fileMeta, "hash"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> getReportImageMap(Report report) {
        Map<String, Map<String, Object>> imageMap = new HashMap<String, Map<String, Object>>();

        for (Map<String, Object> userObject : getReportUserObjects(report)) {
            Map<String, Object> imageMeta = MapUtils.getMap(userObject, "imageMeta");

            if (MapUtils.isNotEmpty(imageMeta)) {
                imageMap.put(MapUtils.getString(imageMeta, "id"), imageMeta);
            }
        }

        return imageMap;
    }

    private Map<String, GridFSDBFile> getReportImageFileMap(Report report) {
        Map<String, GridFSDBFile> imageFileMap = new HashMap<String, GridFSDBFile>();
        Set<String> imageIds = getReportImageMap(report).keySet();
        List<GridFSDBFile> imageFiles = getFiles(imageIds);

        for (GridFSDBFile imageFile : imageFiles) {
            imageFileMap.put(imageFile.getId().toString(), imageFile);
        }

        return imageFileMap;
    }

    private Map<String, Object> storeFile(InputStream is, String fileName, String fileHash) {
        Map<String, Object> fileMeta = new HashMap<String, Object>();

        GridFSFile gridFSFile = mongoGridFsTemplate.store(is, fileName);

        fileMeta.put("id", gridFSFile.getId().toString());
        fileMeta.put("hash", fileHash);

        return fileMeta;
    }

    private List<GridFSDBFile> getFiles(Set<String> ids) {
        return mongoGridFsTemplate.find(query(where("_id").in(ids)));
    }

    private void deleteFiles(Set<String> ids) {
        mongoGridFsTemplate.delete(query(where("_id").in(ids)));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // sparse т.к. сохраненные отчеты не имеют поля shareKey на момент его введения
        mongoTemplate.indexOps(Report.class)//
                .ensureIndex(new Index().on("shareKey", ASC).unique().sparse());
    }
}
