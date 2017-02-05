package ru.nullpointer.nkbrelation.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import ru.nullpointer.nkbrelation.domain.Pair;

/**
 *
 * @author Maksim Konyuhov
 */
@SuppressWarnings("unchecked")
public class ConnectedComponentsTest {

    private static Logger logger = LoggerFactory.getLogger(ConnectedComponentsTest.class);

    @Test
    public void testEmptyInput() {
        Set<Set<Integer>> c = getComponents();

        assertNotNull(c);
        assertTrue(c.isEmpty());
    }

    @Test
    public void testSinglePair() {
        Set<Set<Integer>> c = getComponents(pair(1, 2));

        assertEquals(setOf(setOf(1, 2)), c);
    }

    @Test
    public void testDisconnectedPairs() {
        Set<Set<Integer>> c = getComponents(pair(1, 2), pair(3, 4));

        assertEquals(setOf(setOf(1, 2), setOf(3, 4)), c);
    }

    @Test
    public void testConnectedPairs() {
        Set<Set<Integer>> c = getComponents(pair(1, 2), pair(2, 3));

        assertEquals(setOf(setOf(1, 2, 3)), c);
    }

    @Test
    public void testCycle() {
        Set<Set<Integer>> c = getComponents(pair(1, 2), pair(2, 3), pair(1, 3));

        assertEquals(setOf(setOf(1, 2, 3)), c);
    }

    @Test
    public void testManyConnectedPairs() {
        Set<Set<Integer>> c = getComponents(pair(1, 2), pair(1, 3), pair(5, 6), pair(2, 4), pair(6, 7));

        assertEquals(setOf(setOf(1, 2, 3, 4), setOf(5, 6, 7)), c);
    }

    private Pair<Integer> pair(final Integer first, final Integer second) {
        return new Pair<Integer>() {
            @Override
            public Integer getFirst() {
                return first;
            }

            @Override
            public Integer getSecond() {
                return second;
            }
        };
    }

    private Set<Integer> setOf(Integer... values) {
        return new HashSet<Integer>(Arrays.asList(values));
    }

    private Set<Set<Integer>> setOf(Set<Integer>... values) {
        return new HashSet<Set<Integer>>(Arrays.asList(values));
    }

    private Set<Set<Integer>> getComponents(Pair<Integer>... inputPairs) {
        List<Pair<Integer>> pairs = Arrays.asList(inputPairs);
        return new ConnectedComponents<Integer>(pairs).getComponents();
    }
}
