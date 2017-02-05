package ru.nullpointer.nkbrelation.service;

import org.junit.Test;
import org.neo4j.helpers.collection.MapUtil;
import ru.nullpointer.nkbrelation.domain.BatchTraceResponse;
import ru.nullpointer.nkbrelation.domain.Trace;
import ru.nullpointer.nkbrelation.domain.TraceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author val
 */
public class TraceServiceTest {

    private Trace trace(Integer... nodes) {
        Trace t = new Trace();
        t.setNodes(asList(nodes));
        return t;
    }

    private BatchTraceResponse.Pair.Trace pairTrace(Integer... nodes) {
        return new BatchTraceResponse.Pair.Trace(asList(nodes));
    }

    @Test
    public void addTraceResult2Response() {
        Map<String, Object> node0 = MapUtil.map("_id", "0", "_type", "COMPANY");
        Map<String, Object> node1 = MapUtil.map("_id", "1", "_type", "COMPANY");
        Map<String, Object> node2 = MapUtil.map("_id", "2", "_type", "COMPANY");
        Map<String, Object> node3 = MapUtil.map("_id", "3", "_type", "COMPANY");
        Map<String, Object> node4 = MapUtil.map("_id", "4", "_type", "COMPANY");

        BatchTraceResponse response = new BatchTraceResponse();
        response.setPairs(new ArrayList<BatchTraceResponse.Pair>());
        response.setNodes(new ArrayList<Map<String, Object>>());

        TraceResult result = new TraceResult();
        result.setNodes(asList(node0, node1, node2));
        result.setTraces(asList(trace(0, 1), trace(0, 2, 1)));
        TraceService.addTraceResult2Response(response, result);

        result = new TraceResult();
        result.setNodes(asList(node0, node2, node1));
        result.setTraces(asList(trace(0, 1), trace(0, 2, 1)));
        TraceService.addTraceResult2Response(response, result);

        result = new TraceResult();
        result.setNodes(asList(node3, node2, node4));
        result.setTraces(asList(trace(0, 1), trace(0, 2, 1)));
        TraceService.addTraceResult2Response(response, result);

        List<Map<String,Object>> responseNodesExpected = asList(node0, node1, node2, node3, node4);
        assertThat(response.getNodes()).containsExactlyElementsOf(responseNodesExpected);
        assertThat(response.getPairs()).hasSize(3);

        BatchTraceResponse.Pair pair0 = new BatchTraceResponse.Pair(0, 1, asList(pairTrace(0, 1), pairTrace(0, 2, 1)));
        assertThat(response.getPairs().get(0)).isEqualTo(pair0);

        BatchTraceResponse.Pair pair1 = new BatchTraceResponse.Pair(0, 2, asList(pairTrace(0, 2), pairTrace(0, 1, 2)));
        assertThat(response.getPairs().get(1)).isEqualTo(pair1);

        BatchTraceResponse.Pair pair2 = new BatchTraceResponse.Pair(3, 2, asList(pairTrace(3, 2), pairTrace(3, 4, 2)));
        assertThat(response.getPairs().get(2)).isEqualTo(pair2);
    }
}
