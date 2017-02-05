package ru.nullpointer.nkbrelation.export.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.swing.Java2DRenderer;

import ru.nullpointer.nkbrelation.domain.ExportParams;
import ru.nullpointer.nkbrelation.export.ReportImage;
import ru.nullpointer.nkbrelation.export.ReportImageProducer;
import ru.nullpointer.nkbrelation.support.ImageUtils;
import ru.nullpointer.nkbrelation.xhtmlrenderer.RendererFactory;

/**
 * @author ankostyuk
 * @author "Anton Brok-Volchansky <re6exp@gmail.com>"
 * @author Alexander Yastrebov
 */
public class XHTMLRendererImageProducer implements ReportImageProducer, InitializingBean {

    private static final String DEFAULT_ENCODING = "UTF-8";
    //
    private RendererFactory rendererFactory;

    @Override
    public ReportImage getImage(ExportParams params) throws IOException {
        Document doc = getReportXHTML(params.getReportHTML());
        Double scale = params.getScale();

        BufferedImage trimmed = getReportImage(doc, params.getWidth(), params.getHeight());
        BufferedImage image = trimmed;

        if (scale != null && scale != 1.0) {
            image = ImageUtils.resize(trimmed, (int) (scale * trimmed.getWidth()), (int) (scale * trimmed.getHeight()));
        }

        return new BufferedReportImage(image);
    }

    private Document getReportXHTML(String reportHTML) throws IOException {
        Tidy tidy = buildTidy();

        InputStream is = new ByteArrayInputStream(reportHTML.getBytes(DEFAULT_ENCODING));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = tidy.parseDOM(is, baos);

        is.close();
        baos.close();

        return document;
    }

    private Tidy buildTidy() {
        Tidy tidy = new Tidy();

        tidy.setInputEncoding(DEFAULT_ENCODING);
        tidy.setOutputEncoding(DEFAULT_ENCODING);
        tidy.setXHTML(true);
        tidy.setUpperCaseTags(false);
        tidy.setUpperCaseAttrs(false);
        tidy.setEscapeCdata(false);
        tidy.setTrimEmptyElements(false);
        tidy.setHideComments(true);

        return tidy;
    }

    private BufferedImage getReportImage(Document reportXHTML, int width, int height) {
        Java2DRenderer java2DRenderer = rendererFactory.getRenderer(reportXHTML, width, height);
        BufferedImage image = java2DRenderer.getImage();

        BufferedImage trimmedImage = ImageUtils.trim(image, ImageUtils.WHITE_ARGB);
        return trimmedImage;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(rendererFactory);
    }

    public void setRendererFactory(RendererFactory rendererFactory) {
        this.rendererFactory = rendererFactory;
    }
}
