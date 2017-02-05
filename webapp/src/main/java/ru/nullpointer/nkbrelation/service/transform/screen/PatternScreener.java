package ru.nullpointer.nkbrelation.service.transform.screen;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 *
 * @author ankostyuk
 */
public class PatternScreener extends AbstractScreener implements InitializingBean {

    private static final String PATTERN_SCREEN_TEXT = "$";
    //
    private String value;

    @Override
    public String screen(Object unused) {
        return value;
    }

    public void setPattern(String pattern) {
        Assert.hasText(pattern);
        value = StringUtils.replace(pattern, PATTERN_SCREEN_TEXT, screenValue);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(value, "Pattern must be set");
    }
}
