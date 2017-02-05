package ru.nullpointer.nkbrelation.dao.history;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.dao.algo.FindTracesAlgo;
import ru.nullpointer.nkbrelation.dao.algo.FindTracesParam;
import ru.nullpointer.nkbrelation.domain.NodeId;
import ru.nullpointer.nkbrelation.domain.TraceResult;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.nullpointer.nkbrelation.dao.AlgoTestUtils.getTraces;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/history/FindTracesHistory.xml"
})
public class FindTracesHistoryIT {

    @Resource
    private FindTracesAlgo algo;

    @Test
    public void testFindTracesNoHistoryFlag() {

        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("INDIVIDUAL", "DEMO-INDIVIDUAL-1"));
        param.setSecond(new NodeId("INDIVIDUAL", "DEMO-INDIVIDUAL-2"));
        param.setMaxDepth(4);

        Set<String> filterableTypes = new HashSet<String>();
        filterableTypes.add("FOUNDER_COMPANY");
        filterableTypes.add("FOUNDER_INDIVIDUAL");
        param.setFilterableTypes(filterableTypes);

        TraceResult traceResult = algo.evaluate(param);
        assertEquals(2, traceResult.getTraces().size());

        List<String> list1 = Arrays.asList("INDIVIDUAL.DEMO-INDIVIDUAL-1", "COMPANY.100", "INDIVIDUAL.DEMO-INDIVIDUAL-2");
        List<String> list2 = Arrays.asList("INDIVIDUAL.DEMO-INDIVIDUAL-1", "COMPANY.101", "INDIVIDUAL.DEMO-INDIVIDUAL-2");

        List<List<String>> targetLists = getTraces(traceResult);

        assertTrue(targetLists.contains(list1));
        assertTrue(targetLists.contains(list2));
    }

    @Test
    public void testFindTracesHistoryFlagTrue() {

        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("INDIVIDUAL", "DEMO-INDIVIDUAL-1"));
        param.setSecond(new NodeId("INDIVIDUAL", "DEMO-INDIVIDUAL-2"));
        param.setMaxDepth(4);
        param.setHistoryFlag(true);

        Set<String> filterableTypes = new HashSet<String>();
        filterableTypes.add("FOUNDER_COMPANY");
        filterableTypes.add("FOUNDER_INDIVIDUAL");
        param.setFilterableTypes(filterableTypes);

        TraceResult traceResult = algo.evaluate(param);
        assertEquals(1, traceResult.getTraces().size());

        List<String> list = Arrays.asList("INDIVIDUAL.DEMO-INDIVIDUAL-1", "COMPANY.101", "INDIVIDUAL.DEMO-INDIVIDUAL-2");

        List<List<String>> targetLists = getTraces(traceResult);

        assertTrue(targetLists.contains(list));
    }

    @Test
    public void testFindTracesHistoryFlagFalse() {

        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("INDIVIDUAL", "DEMO-INDIVIDUAL-1"));
        param.setSecond(new NodeId("INDIVIDUAL", "DEMO-INDIVIDUAL-2"));
        param.setMaxDepth(4);
        param.setHistoryFlag(false);

        Set<String> filterableTypes = new HashSet<String>();
        filterableTypes.add("FOUNDER_COMPANY");
        filterableTypes.add("FOUNDER_INDIVIDUAL");
        param.setFilterableTypes(filterableTypes);

        TraceResult traceResult = algo.evaluate(param);
        assertEquals(1, traceResult.getTraces().size());

        List<String> list = Arrays.asList("INDIVIDUAL.DEMO-INDIVIDUAL-1", "COMPANY.100", "INDIVIDUAL.DEMO-INDIVIDUAL-2");

        List<List<String>> targetLists = getTraces(traceResult);

        assertTrue(targetLists.contains(list));
    }
}
