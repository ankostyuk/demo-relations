package ru.nullpointer.nkbrelation.dao.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import ru.nullpointer.nkbrelation.domain.TraceResult;
import ru.nullpointer.nkbrelation.domain.Trace;
import static org.junit.Assert.*;
import static ru.nullpointer.nkbrelation.dao.AlgoTestUtils.getTraces;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.common.NeoIdUtils;
import ru.nullpointer.nkbrelation.domain.NodeId;

/**
 *
 * @author nikolka
 * @author Alexander Yastrebov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/spring/algo/FindTracesAlgo.xml"
})
public class FindTracesAlgoIT {

    @Resource
    private FindTracesAlgo algo;

    @Test
    public void testActualRelation() {
        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("COMPANY", "201"));
        param.setSecond(new NodeId("COMPANY", "203"));
        param.setMaxDepth(1);

        Set<String> filterableTypes = new HashSet<String>();
        filterableTypes.add("FOUNDER_COMPANY");
        param.setFilterableTypes(filterableTypes);

        TraceResult traceResult = algo.evaluate(param);
    }

    @Test
    public void testSourceRelation() {
        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("COMPANY", "200"));
        param.setSecond(new NodeId("COMPANY", "202"));
        param.setMaxDepth(1);

        Set<String> filterableTypes = new HashSet<String>();
        filterableTypes.add("FOUNDER_COMPANY");
        param.setFilterableTypes(filterableTypes);

        TraceResult traceResult = algo.evaluate(param);
    }

    @Test
    public void testFindTraces() {
        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("INDIVIDUAL", "FIND_TR_NODE1"));
        param.setSecond(new NodeId("INDIVIDUAL", "FIND_TR_NODE2"));
        param.setMaxDepth(4);

        Set<String> filterableTypes = new HashSet<String>();
        filterableTypes.add("FOUNDER_COMPANY");
        filterableTypes.add("FOUNDER_INDIVIDUAL");
        param.setFilterableTypes(filterableTypes);

        TraceResult traceResult = algo.evaluate(param);
        assertEquals(2, traceResult.getTraces().size());

        List<String> list1 = Arrays.asList("INDIVIDUAL.FIND_TR_NODE1", "COMPANY.200", "COMPANY.203", "COMPANY.201", "INDIVIDUAL.FIND_TR_NODE2");
        List<String> list2 = Arrays.asList("INDIVIDUAL.FIND_TR_NODE1", "COMPANY.200", "COMPANY.202", "COMPANY.201", "INDIVIDUAL.FIND_TR_NODE2");

        List<List<String>> targetLists = getTraces(traceResult);

        assertTrue(targetLists.contains(list1));
        assertTrue(targetLists.contains(list2));
    }

    @Test
    public void testFilterableType() {
        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("INDIVIDUAL", "FIND_TR_NODE1"));
        param.setSecond(new NodeId("COMPANY", "200"));
        param.setMaxDepth(1);

        Set<String> filterableTypes = new HashSet<String>();
        filterableTypes.add("FOUNDER_COMPANY");
        param.setFilterableTypes(filterableTypes);

        TraceResult traceResult = algo.evaluate(param);
        assertNull(traceResult.getTraces());
    }

    private boolean hasMapWithKeyValue(List<Map<String, Object>> target, String key, Object value) {
        for (Map<String, Object> map : target) {
            Object ob = map.get(key);
            if (ob != null) {
                if (ob.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
