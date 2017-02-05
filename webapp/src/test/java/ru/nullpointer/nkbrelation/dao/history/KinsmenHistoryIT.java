package ru.nullpointer.nkbrelation.dao.history;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.dao.algo.FindKinsmenAlgo;
import ru.nullpointer.nkbrelation.dao.algo.FindKinsmenParam;
import ru.nullpointer.nkbrelation.domain.TraceResult;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.nullpointer.nkbrelation.dao.AlgoTestUtils.getTraces;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/history/KinsmenHistory.xml"
})
public class KinsmenHistoryIT {

    @Resource
    private FindKinsmenAlgo algo;

    @Test
    public void testFindKinsmenNoHistoryFlag() {

        FindKinsmenParam param = new FindKinsmenParam();

        param.setIndividualId("KINSMAN-1");
        param.setMaxDepth(4);

        TraceResult traceResult = algo.evaluate(param);
        assertEquals(2, traceResult.getTraces().size());

        List<String> list1 = Arrays.asList("INDIVIDUAL.KINSMAN-1", "COMPANY.100", "INDIVIDUAL.KINSMAN-2");
        List<String> list2 = Arrays.asList("INDIVIDUAL.KINSMAN-1", "COMPANY.101", "INDIVIDUAL.KINSMAN-3");

        List<List<String>> targetLists = getTraces(traceResult);

        assertTrue(targetLists.contains(list1));
        assertTrue(targetLists.contains(list2));
    }

    @Test
    public void testFindKinsmenHistoryFlagTrue() {

        FindKinsmenParam param = new FindKinsmenParam();

        param.setIndividualId("KINSMAN-1");
        param.setMaxDepth(4);
        param.setHistoryFlag(true);

        TraceResult traceResult = algo.evaluate(param);
        assertEquals(1, traceResult.getTraces().size());

        List<String> list = Arrays.asList("INDIVIDUAL.KINSMAN-1", "COMPANY.101", "INDIVIDUAL.KINSMAN-3");

        List<List<String>> targetLists = getTraces(traceResult);

        assertTrue(targetLists.contains(list));
    }

    @Test
    public void testFindKinsmenHistoryFlagFalse() {

        FindKinsmenParam param = new FindKinsmenParam();

        param.setIndividualId("KINSMAN-1");
        param.setMaxDepth(4);
        param.setHistoryFlag(false);

        TraceResult traceResult = algo.evaluate(param);
        assertEquals(1, traceResult.getTraces().size());

        List<String> list = Arrays.asList("INDIVIDUAL.KINSMAN-1", "COMPANY.100", "INDIVIDUAL.KINSMAN-2");

        List<List<String>> targetLists = getTraces(traceResult);

        assertTrue(targetLists.contains(list));
    }
}
