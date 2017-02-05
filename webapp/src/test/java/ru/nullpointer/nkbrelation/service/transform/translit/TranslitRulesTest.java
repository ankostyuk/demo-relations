package ru.nullpointer.nkbrelation.service.transform.translit;

import com.ibm.icu.text.Transliterator;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Yastrebov
 */
public class TranslitRulesTest { // NOPMD

    private static final Logger logger = LoggerFactory.getLogger(TranslitRulesTest.class); // NOPMD
    //
    private Transliterator transliterator;

    @Before
    public void setUp() throws Exception {
        String rules = IOUtils.toString(TranslitRulesTest.class.getClassLoader().getResourceAsStream("i18n/translit-rules.txt"), "UTF-8");
        transliterator = Transliterator.createFromRules("Russian-Latin/NKB", rules, Transliterator.FORWARD);
    }

    @Test
    public void test_RussianToLatin() {
        // "ЗАКРЫТОЕ АКЦИОНЕРНОЕ ОБЩЕСТВО \"НАЦИОНАЛЬНОЕ КРЕДИТНОЕ БЮРО\""

        assertEquals("Natsionalnoye", tr("Национальное"));
        assertEquals("natsionalnoye", tr("национальное"));
        assertEquals("NATSIONALNOYE", tr("НАЦИОНАЛЬНОЕ"));

        assertEquals("GENERALNY DIREKTOR", tr("ГЕНЕРАЛЬНЫЙ ДИРЕКТОР"));
        assertEquals("STARSHY PARTNER", tr("СТАРШИЙ ПАРТНЕР"));

    }

    private String tr(String s) { // NOPMD
        String result =  transliterator.transliterate(s);
        logger.debug("{} -> {}", s, result);
        return result;
    }
}
