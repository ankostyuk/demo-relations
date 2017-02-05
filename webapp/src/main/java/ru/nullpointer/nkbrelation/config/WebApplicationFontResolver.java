/**
 *
 */
package ru.nullpointer.nkbrelation.config;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.itextpdf.text.FontFactory;

/**
 * Инициализация шрифтов
 *
 * @author "Anton Brok-Volchansky <re6exp@gmail.com>"
 * @author ankostyuk
 */
public class WebApplicationFontResolver implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(WebApplicationFontResolver.class);

    private void init() throws Exception {
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        Resource[] resourceList = pathMatchingResourcePatternResolver.getResources("classpath*:/fonts/*.*tf");

        for (Resource resource : resourceList) {

            // регистрация фонтов в awt
            Font font = Font.createFont(Font.TRUETYPE_FONT, resource.getInputStream());
            ge.registerFont(font);

            // регистрация в iTextPdf
            FontFactory.register("/fonts/" + resource.getFilename(), font.getFontName());

            logger.info("Font named '{}' registered graphics context. Font: {}", font.getFontName(), font);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
