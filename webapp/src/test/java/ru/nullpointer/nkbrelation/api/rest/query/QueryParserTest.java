package ru.nullpointer.nkbrelation.api.rest.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import ru.nullpointer.nkbrelation.domain.meta.Property;
import ru.nullpointer.nkbrelation.domain.meta.SimpleProperty;
import ru.nullpointer.nkbrelation.query.*;

/**
 *
 * @author Alexander Yastrebov
 */
public class QueryParserTest {

    private List<Property> properties;
    private QueryParser parser;

    @Before
    public void init() {
        parser = new QueryParser();

        properties = new ArrayList<Property>();

        properties.add(new SimpleProperty("string1", Property.Type.STRING, true));
        properties.add(new SimpleProperty("string2", Property.Type.STRING, true));
        properties.add(new SimpleProperty("string3", Property.Type.STRING, true));
        properties.add(new SimpleProperty("integer1", Property.Type.INTEGER, true));
        properties.add(new SimpleProperty("integer2", Property.Type.INTEGER, true));
        properties.add(new SimpleProperty("integer3", Property.Type.INTEGER, true));
        properties.add(new SimpleProperty("double1", Property.Type.DOUBLE, true));
        properties.add(new SimpleProperty("double2", Property.Type.DOUBLE, true));
    }

    @Test
    public void containsNoPrefix() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("string1.contains", new String[]{"hello"});
        params.put("string2.contains", new String[]{"world"});

        List<Condition> conditions = parser.parseFilters(null, properties, params);
        assertNotNull(conditions);
        assertEquals(2, conditions.size());

        assertHasContainsCondition("string1", "hello", conditions);
        assertHasContainsCondition("string2", "world", conditions);
    }

    @Test
    public void equalsNoPrefix() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("string1.equals", new String[]{"hello"});
        params.put("string2.equals", new String[]{"world"});

        List<Condition> conditions = parser.parseFilters(null, properties, params);
        assertNotNull(conditions);
        assertEquals(2, conditions.size());

        assertHasEqualsCondition("string1", "hello", true, conditions);
        assertHasEqualsCondition("string2", "world", true, conditions);
    }

    @Test
    public void notEquals() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("string1.not_equals", new String[]{"hello"});
        params.put("string2.not_equals", new String[]{"world"});

        List<Condition> conditions = parser.parseFilters(null, properties, params);
        assertNotNull(conditions);
        assertEquals(2, conditions.size());

        assertHasEqualsCondition("string1", "hello", false, conditions);
        assertHasEqualsCondition("string2", "world", false, conditions);
    }

    @Test
    public void existsNoPrefix() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("string1.exists", new String[]{"true"});
        params.put("string2.exists", new String[]{"false"});

        List<Condition> conditions = parser.parseFilters(null, properties, params);
        assertNotNull(conditions);
        assertEquals(2, conditions.size());

        assertHasExistsCondition("string1", true, conditions);
        assertHasExistsCondition("string2", false, conditions);
    }

    @Test
    public void integerMinMaxNoPrefix() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("integer1.min", new String[]{"10"});
        params.put("integer1.max", new String[]{"100"});
        params.put("integer2.min", new String[]{"1000"});
        params.put("integer3.max", new String[]{"10000"});

        List<Condition> conditions = parser.parseFilters(null, properties, params);
        assertNotNull(conditions);
        assertEquals(3, conditions.size());

        assertHasIntegerMinMax("integer1", 10L, 100L, conditions);
        assertHasIntegerMinMax("integer2", 1000L, null, conditions);
        assertHasIntegerMinMax("integer3", null, 10000L, conditions);
    }

    @Test
    public void containsPrefix() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("prefix.string1.contains", new String[]{"hello"});
        params.put("prefix.string2.contains", new String[]{"world"});

        List<Condition> conditions = parser.parseFilters("prefix", properties, params);
        assertNotNull(conditions);
        assertEquals(2, conditions.size());

        assertHasContainsCondition("string1", "hello", conditions);
        assertHasContainsCondition("string2", "world", conditions);
    }

    @Test
    public void containsNoPrefixMultiple() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("string1.contains", new String[]{"hello", "bye"});
        params.put("string2.contains", new String[]{"world", "home"});

        List<Condition> conditions = parser.parseFilters(null, properties, params);
        assertNotNull(conditions);
        assertEquals(4, conditions.size());

        assertHasContainsCondition("string1", "hello", conditions);
        assertHasContainsCondition("string1", "bye", conditions);
        assertHasContainsCondition("string2", "world", conditions);
        assertHasContainsCondition("string2", "home", conditions);
    }

    @Test
    public void testContainsWithSpaces() {
        final String str = "   aaa    bbb       ";

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("string1.contains", new String[]{str});

        List<Condition> conditions = parser.parseFilters(null, properties, params);
        assertNotNull(conditions);
        assertEquals(1, conditions.size());

        assertHasContainsCondition("string1", str, conditions);
    }

    @Test
    public void testSearch() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("q", new String[]{"hello"});

        SearchCondition search = parser.parseSearch(Arrays.asList("q", "qh"), params);
        assertNotNull(search);

        assertEquals("q", search.getType());
        assertEquals("hello", search.getText());
    }

    @Test
    public void testUnsupportedSearch() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("unsupported", new String[]{"hello"});

        SearchCondition search = parser.parseSearch(Arrays.asList("q"), params);
        assertNull(search);
    }

    private void assertHasIntegerMinMax(String name, Long min, Long max, List<Condition> conditions) {
        boolean success = false;
        for (Condition c : conditions) {
            if (c instanceof MinMaxCondition) {
                MinMaxCondition cc = (MinMaxCondition) c;
                if (cc.getName().equals(name)) {
                    assertFalse(success);
                    if (min != null) {
                        assertEquals(cc.getMin(), min);
                    } else {
                        assertNull(cc.getMin());
                    }
                    if (max != null) {
                        assertEquals(cc.getMax(), max);
                    } else {
                        assertNull(cc.getMax());
                    }

                    success = true;
                }
            }
        }
        assertTrue(success);
    }

    private void assertHasContainsCondition(String name, String value, List<Condition> conditions) {
        boolean success = false;
        for (Condition c : conditions) {
            if (c instanceof ContainsCondition) {
                ContainsCondition cc = (ContainsCondition) c;
                if (cc.getName().equals(name) && cc.getValue().equals(value)) {
                    success = true;
                    break;
                }
            }
        }
        assertTrue(success);
    }

    private void assertHasEqualsCondition(String name, String value, boolean isEquals, List<Condition> conditions) {
        boolean success = false;
        for (Condition c : conditions) {
            if (c instanceof EqualsAnyCondition) {
                EqualsAnyCondition cc = (EqualsAnyCondition) c;
                List<String> values = cc.getValues();
                assertNotNull(values);
                assertTrue(values.size() == 1);

                if (cc.getName().equals(name)
                        && values.get(0).equals(value)
                        && cc.isEquals() == isEquals) {
                    success = true;
                    break;
                }
            }
        }
        assertTrue(success);
    }

    private void assertHasExistsCondition(String name, boolean value, List<Condition> conditions) {
        boolean success = false;
        for (Condition c : conditions) {
            if (c instanceof ExistsCondition) {
                ExistsCondition cc = (ExistsCondition) c;
                if (cc.getName().equals(name) && cc.isExists() == value) {
                    success = true;
                    break;
                }
            }
        }
        assertTrue(success);
    }
}
