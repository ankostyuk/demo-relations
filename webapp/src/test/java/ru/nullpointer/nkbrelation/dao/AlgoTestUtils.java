package ru.nullpointer.nkbrelation.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nullpointer.nkbrelation.common.NeoIdUtils;
import ru.nullpointer.nkbrelation.domain.Trace;
import ru.nullpointer.nkbrelation.domain.TraceResult;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: rainbow
 * Date: 03.02.16 21:17
 */

public class AlgoTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(AlgoTestUtils.class);

    public static void assertValidEndBeneficiaries(TraceResult traceResult, String... ids) {
        List<Map<String, Object>> endBeneficiaries = getEndNodes(traceResult);

        logger.debug("result: {}, end: {}", traceResult, endBeneficiaries);

        assertEquals(ids.length, endBeneficiaries.size());
        List<String> copy = new LinkedList<String>(Arrays.asList(ids));
        for (Map<String, Object> m : endBeneficiaries) {
            assertEquals("INDIVIDUAL", m.get("_type"));
            assertTrue(copy.remove(m.get("_id").toString()));
        }
        assertTrue(copy.isEmpty());
    }

    public static void assertValidTraces(TraceResult traceResult, Integer maxDepth) {
        int prevLen = Integer.MIN_VALUE;
        for (Trace trace : traceResult.getTraces()) {
            int traceLength = trace.getNodes().size();

            assertTrue(traceLength >= prevLen);
            if (maxDepth != null) {
                assertTrue(traceLength <= maxDepth);
            }
            for (int i = 1; i < traceLength - 1; i++) {
                Map<String, Object> node = getTraceNode(traceResult, trace, i);
                assertEquals("COMPANY", node.get("_type"));
            }
            prevLen = traceLength;
        }
    }

    public static List<Map<String, Object>> getEndNodes(TraceResult traceResult) {
        List<Trace> traces = traceResult.getTraces();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Trace trace : traces) {
            Map<String, Object> node = getTraceNode(traceResult, trace, trace.getNodes().size() - 1);

            result.add(node);
        }
        return result;
    }

    public static Map<String, Object> getTraceNode(TraceResult traceResult, Trace trace, int index) {
        Integer nodeIndex = trace.getNodes().get(index);
        return traceResult.getNodes().get(nodeIndex);
    }

    public static List<List<String>> getTraces(TraceResult traceResult) {
        List<List<String>> listTraces = new ArrayList<List<String>>();
        for (Trace trace : traceResult.getTraces()) {
            List<String> listNodeNames = new ArrayList<String>();
            for (int i = 0; i < trace.getNodes().size(); i++) {
                int indexNode = trace.getNodes().get(i);
                Map<String, Object> node = traceResult.getNodes().get(indexNode);
                listNodeNames.add(NeoIdUtils.compoundId(node.get("_type").toString(), node.get("_id").toString()));
            }
            listTraces.add(listNodeNames);
        }
        return listTraces;
    }
}
