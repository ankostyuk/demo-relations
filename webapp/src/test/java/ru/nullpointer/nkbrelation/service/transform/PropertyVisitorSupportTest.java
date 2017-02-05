package ru.nullpointer.nkbrelation.service.transform;

import ru.nullpointer.nkbrelation.common.transform.PropertyVisitorSupport;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Alexander Yastrebov
 */
public class PropertyVisitorSupportTest { // NOPMD

    private static final Logger logger = LoggerFactory.getLogger(PropertyVisitorSupportTest.class); // NOPMD
    //
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    @Test
    public void testSimple() {
        Map<String, Object> m = parse("{firstname:'alexander', lastname:'yastrebov'}");

        final MutableBoolean visited = new MutableBoolean(false);

        new PropertyVisitorSupport(new String[]{"firstname", "lastname"}) {
            @Override
            protected Object visitValue(String key, Object value) {
                if ("firstname".equals(key)) {
                    assertEquals("alexander", value);
                } else if ("lastname".equals(key)) {
                    assertEquals("yastrebov", value);
                } else {
                    fail("Unexpected key and value: " + key + "," + value);
                }
                visited.setValue(true);
                return null;
            }
        }.visit(m);
        assertTrue("No property was visited", visited.getValue());
    }

    @Test
    public void testCommonPrefix() {
        Map<String, Object> m = parse("{nam:'a', name:'x', name1:'y', name2:'z'}");

        final MutableBoolean visited = new MutableBoolean(false);

        new PropertyVisitorSupport(new String[]{"name"}) {
            @Override
            protected Object visitValue(String key, Object value) {
                if ("name".equals(key)) {
                    assertEquals("x", value);
                } else {
                    fail("Unexpected key and value: " + key + "," + value);
                }
                visited.setValue(true);
                return null;
            }
        }.visit(m);
        assertTrue("No property was visited", visited.getValue());
    }

    @Test
    public void testCommonPrefixNested() {
        Map<String, Object> m = parse("{n:'a', name:{x:'x', y:'y'}, namey:'y', namey:'z'}");

        final MutableBoolean visited = new MutableBoolean(false);

        new PropertyVisitorSupport(new String[]{"name.x"}) {
            @Override
            protected Object visitValue(String key, Object value) {
                if ("name.x".equals(key)) {
                    assertEquals("x", value);
                } else {
                    fail("Unexpected key and value: " + key + "," + value);
                }
                visited.setValue(true);
                return null;
            }
        }.visit(m);
        assertTrue("No property was visited", visited.getValue());
    }

    @Test
    public void testNested() {
        Map<String, Object> m = parse("{name:{x:'x', y:'y'}}");

        final MutableBoolean visited = new MutableBoolean(false);

        new PropertyVisitorSupport(new String[]{"name"}) {
            @Override
            protected Object visitValue(String key, Object value) {
                visited.setValue(true);
                return null;
            }
        }.visit(m);
        assertFalse("No property is expected to be visited", visited.getValue());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String s) {
        try {
            return (Map<String, Object>) mapper.readValue(s, Map.class);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
