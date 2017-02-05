package ru.nullpointer.nkbrelation.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.nullpointer.nkbrelation.domain.ExportParams;
import ru.nullpointer.nkbrelation.domain.report.Report;
import ru.nullpointer.nkbrelation.export.ExportException;
import ru.nullpointer.nkbrelation.export.ReportExporter;
import ru.nullpointer.nkbrelation.export.ReportImage;
import ru.nullpointer.nkbrelation.export.ReportImageProducer;
import ru.nullpointer.nkbrelation.export.docx.DocxReportExporter;
import ru.nullpointer.nkbrelation.export.pdf.PdfReportExporter;
import static ru.nullpointer.nkbrelation.service.ValidationUtils.*;
import ru.nullpointer.nkbrelation.service.security.Permissions;
import ru.nullpointer.nkbrelation.service.security.SecurityService;

/**
 * @author ankostyuk
 * @author "Anton Brok-Volchansky <re6exp@gmail.com>"
 * @author Alexander Yastrebov
 */
@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    //
    private final Map<ExportParams.Format, String> permissions;
    private final Map<ExportParams.Format, ReportExporter> exporters;

    //
    @Value("${relations.export.canvas.width.value.max:6400}")
    private int exportImageMaxWidth;
    //
    @Value("${relations.export.canvas.height.value.max:6400}")
    private int exportImageMaxHeight;
    //
    @Resource(name = "internalReportImageProducer")
    private ReportImageProducer internalProducer;
    //
    @Resource(name = "externalReportImageProducer")
    private ReportImageProducer externalProducer;
    //
    @Resource
    private ReportService reportService;
    //
    @Resource
    private SecurityService securityService;

    public ExportService() {
        permissions = new EnumMap<ExportParams.Format, String>(ExportParams.Format.class);

        permissions.put(ExportParams.Format.PNG, Permissions.REPORT_EXPORT_IMAGE.name());
        permissions.put(ExportParams.Format.PDF, Permissions.REPORT_EXPORT_PDF.name());
        permissions.put(ExportParams.Format.DOCX, Permissions.REPORT_EXPORT_DOCX.name());

        exporters = new EnumMap<ExportParams.Format, ReportExporter>(ExportParams.Format.class);
        exporters.put(ExportParams.Format.PDF, new PdfReportExporter());
        exporters.put(ExportParams.Format.DOCX, new DocxReportExporter());
    }

    public void export(ExportParams params, OutputStream os) {
        ExportParams.Format format = params.getFormat();
        checkNotNull(format, "Format required");
        checkImageRequired(params);

        String permission = permissions.get(format);
        Assert.notNull(permission);
        securityService.ensureHasPermission(permission);

        ReportImage reportImage = getReportImage(params);

        if (format == ExportParams.Format.PNG) {
            try {
                reportImage.writeTo(os);
            } catch (IOException e) {
                throw new ServiceException("Failed to write report image", e);
            }
        } else {
            checkDocumentRequired(params);

            ReportExporter exporter = exporters.get(format);
            Assert.notNull(exporter);

            Report report = reportService.getReport(params.getReportId());
            try {
                exporter.export(report, reportImage, params, os);
            } catch (ExportException ex) {
                throw new ServiceException("Export failed", ex);
            }
        }
    }

    private ReportImage getReportImage(ExportParams params) {
        ReportImageProducer producer = "external".equals(params.getRenderer()) ? externalProducer : internalProducer;

        setCheckedImageSize(params);

        try {
            return producer.getImage(params);
        } catch (IOException e) {
            throw new ServiceException("Failed to get report image", e);
        }
    }

    private void setCheckedImageSize(ExportParams params) {
        if (params.getWidth() * params.getHeight() <= exportImageMaxWidth * exportImageMaxHeight) {
            return;
        }

        if (params.getWidth() > exportImageMaxWidth) {
            params.setWidth(exportImageMaxWidth);
        }

        if (params.getHeight() > exportImageMaxHeight) {
            params.setHeight(exportImageMaxHeight);
        }
    }

    private void checkImageRequired(ExportParams params) {
        checkNotBlank(params.getReportId(), "reportId required");
        checkNotBlank(params.getReportHTML(), "reportHTML required");

        checkNotNull(params.getWidth(), "width required");
        checkNotNull(params.getHeight(), "height required");

        checkIsTrue(params.getWidth() > 0, "width must be positive");
        checkIsTrue(params.getHeight() > 0, "height must be positive");

        checkNotNull(params.getLang(), "lang required");
        checkNotNull(params.getUi(), "ui required");
    }

    private void checkDocumentRequired(ExportParams params) {
        checkNotNull(params.getPageSize(), "pageSize required");
        checkNotNull(params.isCutDown(), "cutDown required");
    }
}
