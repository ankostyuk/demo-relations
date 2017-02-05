package ru.nullpointer.nkbrelation.service;

import java.security.SecureRandom;
import java.util.*;
import javax.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.nullpointer.nkbrelation.dao.ReportDAO;
import ru.nullpointer.nkbrelation.domain.NodeId;
import ru.nullpointer.nkbrelation.domain.meta.NodeType;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;
import ru.nullpointer.nkbrelation.domain.report.Link;
import ru.nullpointer.nkbrelation.domain.report.Report;
import static ru.nullpointer.nkbrelation.service.ValidationUtils.*;
import ru.nullpointer.nkbrelation.service.security.Permissions;
import ru.nullpointer.nkbrelation.service.security.SecurityService;
import ru.nullpointer.nkbrelation.service.transform.LanguageContextFactory;
import ru.nullpointer.nkbrelation.support.GraphUtils;

/**
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
@Service
public class ReportService implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(ReportService.class);
    //
    private static final Random RANDOM = new SecureRandom();
    //
    private static final String USER_OBJECT_NODE_TYPE = "USER_OBJECT";
    //
    @Resource
    private SecurityService securityService;
    //
    @Resource
    private TimeService timeService;
    //
    @Resource
    private GraphService graphService;
    //
    @Resource
    private ReportDAO reportDAO;
    //
    private Set<String> validNodeTypes;
    private Set<String> validRelTypes;

    public List<Report> getReports() {
        securityService.ensureHasPermission(Permissions.REPORT_LIST.name());

        String userId = securityService.getAuthenticatedUserId();
        return reportDAO.getListByUserId(userId);
    }

    public Report getReport(String id) {
        securityService.ensureHasPermission(Permissions.REPORT_VIEW.name());

        Report report = getReportInternal(id);

        String userId = securityService.getAuthenticatedUserId();
        ensureOwner(report, userId);

        initSharingIfNone(report);

        return report;
    }

    public Report getSharedReport(String key) {
        Report report = findReportByShareKey(key);

        fillGraphData(report);

        return report;
    }

    public Report saveReport(Report report) {
        securityService.ensureHasPermission(Permissions.REPORT_SAVE.name());

        String userId = securityService.getAuthenticatedUserId();
        String id = report.getId();

        Report stored;
        boolean touch = false;

        if (id == null) {
            checkNotBlank(report.getName(), "Name must be set");
            stored = initReport(userId);
            touch = true;
        } else {
            stored = findReportById(id);
            ensureOwner(stored, userId);
        }

        if (report.getName() != null) {
            stored.setName(report.getName());
            touch = true;
        }
        if (report.getComment() != null) {
            stored.setComment(report.getComment());
            touch = true;
        }
        if (report.getShared() != null) {
            stored.setShared(report.getShared());
            // таймстемп не трогаем
        }

        if (report.getStyle() != null) {
            stored.setStyle(report.getStyle());
            touch = true;
        }

        if (report.getNodes() != null) {
            validateNodes(report);
            stored.setNodes(nullify(report.getNodes()));
            touch = true;
        }
        if (report.getRelations() != null) {
            validateRelations(report);
            stored.setRelations(nullify(report.getRelations()));
            touch = true;
        }
        if (report.getLinks() != null) {
            validateLinks(report);
            stored.setLinks(nullify(report.getLinks()));
            touch = true;
        }

        if (report.getUserObjects() != null) {
            // TODO validate UserObjects
            stored.setUserObjects(nullify(report.getUserObjects()));
            touch = true;
        }

        if (touch) {
            stored.setEdited(timeService.now());
        }

        if (id == null) {
            reportDAO.insert(stored);
        } else {
            reportDAO.update(stored);
        }
        // вернуть только частичную информацию об отчете
        stored.setNodes(null);
        stored.setRelations(null);
        stored.setLinks(null);
        stored.setUserObjects(null);

        return stored;
    }

    public void deleteReport(String id) {
        securityService.ensureHasPermission(Permissions.REPORT_DELETE.name());

        String userId = securityService.getAuthenticatedUserId();
        Report stored = findReportById(id);

        ensureOwner(stored, userId);

        reportDAO.delete(id);
    }

    // internal api
    public Report getReportInternal(String id) {
        Report report = findReportById(id);

        fillGraphData(report);

        return report;
    }

    private void validateNodes(Report report) {
        List<Map<String, Object>> nodes = report.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (Map<String, Object> node : nodes) {
            checkNotNull(node, "Node is null");

            String[] required = {"_id", "_type", "_x", "_y"};
            checkNotBlankValues(node, required);

            String type = node.get("_type").toString();
            checkIsTrue(validNodeTypes.contains(type), "Invalid node type " + type);

            int size = required.length + optionalCount(node, "_comment", "_style");
            checkIsTrue(node.size() == size, "Extra data in node " + node);
        }
    }

    private void validateRelations(Report report) {
        List<Map<String, Object>> relations = report.getRelations();
        if (relations == null || relations.isEmpty()) {
            return;
        }
        for (Map<String, Object> rel : relations) {
            checkNotNull(rel, "Relation is null");

            String[] required = {"_srcId", "_dstId", "_type"};
            checkNotBlankValues(rel, required);

            String type = rel.get("_type").toString();
            checkIsTrue(validRelTypes.contains(type), "Invalid relation type " + type);

            int size = required.length + optionalCount(rel, "_comment", "_style");
            checkIsTrue(rel.size() == size, "Extra data in relation " + rel);
        }
    }

    private void validateLinks(Report report) {
        List<Link> links = report.getLinks();
        if (links == null || links.isEmpty()) {
            return;
        }
        for (Link link : links) {
            checkNotNull(link, "Link is null");
            checkNotBlank(link.getComment(), "Comment must be set");

            List<NodeId> nodes = link.getNodes();
            checkNotNull(nodes, "Nodes is null");
            checkIsTrue(nodes.size() > 1, "Nodes length must be greater than one");

            for (NodeId n : nodes) {
                checkNotNull(n, "Node is null");
                checkNotBlank(n.getId(), "Id must be set");
                checkNotBlank(n.getType(), "Type must be set");

                String type = n.getType();
                checkIsTrue(validNodeTypes.contains(type), "Invalid node type " + type);
            }
        }
    }

    private Report findReportById(String id) {
        Report report = reportDAO.getById(id);
        if (report == null) {
            throw new NotFoundException("Report with id '" + id + "' not found");
        }
        return report;
    }

    private Report findReportByShareKey(String key) {
        Report report = reportDAO.getByShareKey(key);
        if (report == null || !report.getShared()) {
            throw new NotFoundException("Report with share key '" + key + "' not found");
        }
        return report;
    }

    private Report initReport(String userId) {
        Report r = new Report();
        r.setUserId(userId);
        r.setCreated(timeService.now());
        r.setShareKey(generateShareKey());
        r.setShared(false);
        return r;
    }

    private void ensureOwner(Report report, String userId) {
        if (!userId.equals(report.getUserId())) {
            securityService.accessDenied(null);
        }
    }

    private void fillGraphData(Report report) {
        StopWatch sw = new StopWatch("fillGraphData");
        sw.start("fillNodesData");

        fillNodesData(report);

        sw.stop();
        sw.start("fillRelationsData");

        fillRelationsData(report);

        sw.stop();
        logger.info("{}", sw);

        filterLinks(report);

        // TODO filter User Objects
    }

    private void fillNodesData(Report report) {
        List<Map<String, Object>> nodes = report.getNodes();
        if (nodes == null) {
            return;
        }

        Map<NodeId, Map<String, Object>> nodesMap = new HashMap<NodeId, Map<String, Object>>(nodes.size());
        for (Map<String, Object> n : nodes) {
            nodesMap.put(GraphUtils.nodeId(n), n);
        }

        List<Map<String, Object>> nodesData = graphService.findNodes(new ArrayList<NodeId>(nodesMap.keySet()), LanguageContextFactory.INSTANCE.getContext());
        if (nodesData.isEmpty()) {
            report.setNodes(null);
        } else {
            if (nodesData.size() != nodes.size()) {
                logger.warn("Report nodes count differs from actual nodes count. Report nodes: {}, actual nodes: {}",
                        nodes, nodesData);
            }
            for (Map<String, Object> n : nodesData) {
                Map<String, Object> node = nodesMap.get(GraphUtils.nodeId(n));
                copy(n, node, "_x", "_y", "_comment", "_style");
            }
            report.setNodes(nodesData);
        }
    }

    private void fillRelationsData(Report report) {
        if (report.getNodes() == null) {
            report.setRelations(null);
            return;
        }

        List<Map<String, Object>> rels = report.getRelations();
        if (rels == null) {
            return;
        }

        Map<String, Map<String, Object>> relsMap = new HashMap<String, Map<String, Object>>(rels.size());
        for (Map<String, Object> r : rels) {
            relsMap.put(GraphUtils.relKey(r), r);
        }

        List<Map<String, Object>> relsData = new ArrayList<Map<String, Object>>(rels.size());
        for (Map<String, Object> node : report.getNodes()) {

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodeRels = (List<Map<String, Object>>) node.get("_relations");
            if (nodeRels == null) {
                continue;
            }
            for (Map<String, Object> nodeRel : nodeRels) {
                String k = GraphUtils.relKey(nodeRel);
                Map<String, Object> r = relsMap.remove(k);
                if (r != null) {
                    Map<String, Object> relCopy = new HashMap<String, Object>(nodeRel);
                    copy(relCopy, r, "_comment", "_style");
                    relsData.add(relCopy);
                }
            }
        }

        // скопировать отсутствующие связи с пометкой об удалении
        for (Map<String, Object> deleted : relsMap.values()) {
            deleted.put("_deleted", true);
            relsData.add(deleted);
        }

        if (!relsData.isEmpty()) {
            report.setRelations(relsData);
        } else {
            report.setRelations(null);
        }
    }

    private void filterLinks(Report report) {
        List<Link> links = report.getLinks();
        if (links == null) {
            return;
        }

        // Отменить фильтрацию по нахождению нод в отчете, пока:
        // TODO Реализовать проверку связей-заметок с пользовательскими объектами

        if (links.isEmpty()) {
            report.setLinks(null);
        }
    }

    private <T> List<T> nullify(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }

    private int optionalCount(Map<String, Object> m, String... keys) {
        int count = 0;
        for (String k : keys) {
            if (m.containsKey(k)) {
                count++;
            }
        }
        return count;
    }

    private void copy(Map<String, Object> dst, Map<String, Object> src, String... keys) {
        for (String k : keys) {
            dst.put(k, src.get(k));
        }
    }

    private void initSharingIfNone(Report report) {
        if (report.getShareKey() != null) {
            return;
        }
        report.setShareKey(generateShareKey());
        report.setShared(false);

        reportDAO.update(report);
    }

    private String generateShareKey() {
        return RandomStringUtils.random(16, 'a', 'z' + 1, false, false, null, RANDOM);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validNodeTypes = new HashSet<String>();
        for (NodeType type : graphService.getNodeTypeList()) {
            validNodeTypes.add(type.getName());
        }
        validNodeTypes.add(USER_OBJECT_NODE_TYPE);
        validNodeTypes = Collections.unmodifiableSet(validNodeTypes);

        validRelTypes = new HashSet<String>();
        for (RelationType type : graphService.getRelationTypeList()) {
            validRelTypes.add(type.getName());
        }
        validRelTypes = Collections.unmodifiableSet(validRelTypes);
    }
}
