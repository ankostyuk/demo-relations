package ru.nullpointer.nkbrelation.dao.impl;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.dao.algo.FindTracesAlgo;
import ru.nullpointer.nkbrelation.dao.algo.FindTracesParam;
import ru.nullpointer.nkbrelation.domain.NodeId;
import ru.nullpointer.nkbrelation.domain.TraceResult;

/**
 *
 * @author Maksim Konyuhov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/spring/algo/FindTracesCountRelations.xml"
})
public class FindTracesCountRelationsIT {

    private static final Logger logger = LoggerFactory.getLogger(FindTracesCountRelationsIT.class);

    @Resource
    private FindTracesAlgo algo;

    @Test
    public void testCountRelations() {
        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("COMPANY", "1"));
        param.setSecond(new NodeId("COMPANY", "3"));
        param.setMaxDepth(3);

        TraceResult traceResult = algo.evaluate(param);
        logger.debug("traceResult: {}", traceResult);
        assertEquals(2, traceResult.getTraces().size());
    }

    @Test
    public void testStartNodeLimits() {
        // 2904 - Не фильтровать стартовые ноды по количеству связей при поиске между двумя
        FindTracesParam param = new FindTracesParam();

        param.setFirst(new NodeId("COMPANY", "2"));
        param.setSecond(new NodeId("COMPANY", "6"));
        param.setMaxDepth(3);

        TraceResult traceResult = algo.evaluate(param);
        logger.debug("traceResult: {}", traceResult);
        assertEquals(2, traceResult.getTraces().size());
    }
}
