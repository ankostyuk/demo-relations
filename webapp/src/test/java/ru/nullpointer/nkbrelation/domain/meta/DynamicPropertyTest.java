package ru.nullpointer.nkbrelation.domain.meta;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Alexander Yastrebov
 */
public class DynamicPropertyTest {

    @Test
    public void noPattern() {
        DynamicProperty p = new DynamicProperty();
        p.setName("test");

        assertTrue(p.matchesName("test"));

        assertFalse(p.matchesName("test1"));
        assertFalse(p.matchesName("1test"));
        assertFalse(p.matchesName("hello"));
    }

    @Test
    public void patternLast() {
        DynamicProperty p = new DynamicProperty();
        p.setName("test*");

        assertTrue(p.matchesName("test"));
        assertTrue(p.matchesName("test1"));
        assertTrue(p.matchesName("test_test"));

        assertFalse(p.matchesName("1test"));
        assertFalse(p.matchesName("hello"));
    }

    @Test
    public void patternFirst() {
        DynamicProperty p = new DynamicProperty();
        p.setName("*test");

        assertTrue(p.matchesName("test"));
        assertTrue(p.matchesName("1test"));
        assertTrue(p.matchesName("sometest"));
        assertTrue(p.matchesName("test_test"));

        assertFalse(p.matchesName("_test1"));
        assertFalse(p.matchesName("hello"));

    }

    @Test
    public void patternMiddle() {
        DynamicProperty p = new DynamicProperty();
        p.setName("hello*world");

        assertTrue(p.matchesName("helloworld"));
        assertTrue(p.matchesName("hello_world"));

        assertFalse(p.matchesName("helloworld__"));
        assertFalse(p.matchesName("__helloworld"));
    }
}
