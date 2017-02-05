package ru.nullpointer.nkbrelation.support;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Yastrebov
 */
public class CompareUtilsTest {

    @Test
    public void testObjectComparatorAscendingNullsLast() {
        CompareUtils.ObjectComparator c = new CompareUtils.ObjectComparator(true, true);

        assertTrue(c.compare(null, null) == 0);

        assertTrue(c.compare(1L, null) < 0);
        assertTrue(c.compare(null, 1L) > 0);
        assertTrue(c.compare("A", null) < 0);
        assertTrue(c.compare(null, "A") > 0);

        assertTrue(c.compare(1L, 1L) == 0);
        assertTrue(c.compare(1L, 2L) < 0);
        assertTrue(c.compare("A", "A") == 0);
        assertTrue(c.compare("A", "B") < 0);
    }

    @Test
    public void testObjectComparatorDescendingNullsLast() {
        CompareUtils.ObjectComparator c = new CompareUtils.ObjectComparator(false, true);

        assertTrue(c.compare(null, null) == 0);

        assertTrue(c.compare(1L, null) < 0);
        assertTrue(c.compare(null, 1L) > 0);
        assertTrue(c.compare("A", null) < 0);
        assertTrue(c.compare(null, "A") > 0);

        assertTrue(c.compare(1L, 1L) == 0);
        assertTrue(c.compare(1L, 2L) > 0);
        assertTrue(c.compare("A", "A") == 0);
        assertTrue(c.compare("A", "B") > 0);
    }

    @Test
    public void testObjectComparatorAscendingNullsFirst() {
        CompareUtils.ObjectComparator c = new CompareUtils.ObjectComparator(true, false);

        assertTrue(c.compare(null, null) == 0);

        assertTrue(c.compare(1L, null) > 0);
        assertTrue(c.compare(null, 1L) < 0);
        assertTrue(c.compare("A", null) > 0);
        assertTrue(c.compare(null, "A") < 0);

        assertTrue(c.compare(1L, 1L) == 0);
        assertTrue(c.compare(1L, 2L) < 0);
        assertTrue(c.compare("A", "A") == 0);
        assertTrue(c.compare("A", "B") < 0);
    }

    @Test
    public void testObjectComparatorDescendingNullsFirst() {
        CompareUtils.ObjectComparator c = new CompareUtils.ObjectComparator(false, false);

        assertTrue(c.compare(null, null) == 0);

        assertTrue(c.compare(1L, null) > 0);
        assertTrue(c.compare(null, 1L) < 0);
        assertTrue(c.compare("A", null) > 0);
        assertTrue(c.compare(null, "A") < 0);

        assertTrue(c.compare(1L, 1L) == 0);
        assertTrue(c.compare(1L, 2L) > 0);
        assertTrue(c.compare("A", "A") == 0);
        assertTrue(c.compare("A", "B") > 0);
    }
}
