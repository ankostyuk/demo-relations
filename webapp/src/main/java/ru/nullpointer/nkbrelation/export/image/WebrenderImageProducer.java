package ru.nullpointer.nkbrelation.export.image;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.util.UriTemplate;

import ru.nullpointer.nkbrelation.domain.ExportParams;
import ru.nullpointer.nkbrelation.export.ReportImage;
import ru.nullpointer.nkbrelation.export.ReportImageProducer;

/**
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public class WebrenderImageProducer implements ReportImageProducer, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(WebrenderImageProducer.class);
    //
    private String webrenderUrlTemplate;
    private UriTemplate uriTemplate;

    @Override
    public ReportImage getImage(ExportParams params) throws IOException {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("reportId", params.getReportId());
        vars.put("width", params.getWidth());
        vars.put("height", params.getHeight());
        vars.put("scale", params.getScale() != null ? params.getScale() : 1);
        vars.put("lang", params.getLang());
        vars.put("ui", params.getUi());

        URI uri = uriTemplate.expand(vars);

        logger.debug("Get report image from url: {}", uri);

        return new URLReportImage(uri.toURL());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(webrenderUrlTemplate);

        uriTemplate = new UriTemplate(webrenderUrlTemplate);
    }

    public void setWebrenderUrlTemplate(String webrenderUrlTemplate) {
        this.webrenderUrlTemplate = webrenderUrlTemplate;
    }
}
