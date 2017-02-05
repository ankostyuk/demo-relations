package ru.nullpointer.nkbrelation.dao.history;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.dao.algo.IndividualBeneficiaryAlgo;
import ru.nullpointer.nkbrelation.dao.algo.IndividualBeneficiaryParam;
import ru.nullpointer.nkbrelation.domain.TraceResult;

import javax.annotation.Resource;

import static ru.nullpointer.nkbrelation.dao.AlgoTestUtils.assertValidEndBeneficiaries;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/history/IndividualBeneficiaryHistory.xml"
})
public class IndividualBeneficiaryHistoryIT {

    private final Logger logger = LoggerFactory.getLogger(IndividualBeneficiaryHistoryIT.class);

    @Resource
    private IndividualBeneficiaryAlgo algo;

    @Test
    public void testNoHistoryFlag() {

        IndividualBeneficiaryParam param = new IndividualBeneficiaryParam();
        param.setCompanyId("100");
        param.setMaxDepth(1);

        TraceResult traceResult = algo.evaluate(param);

        assertValidEndBeneficiaries(traceResult, "DEMO-INDIVIDUAL-1", "DEMO-INDIVIDUAL-2");
    }

    @Test
    public void testHistoryFlagTrue() {

        IndividualBeneficiaryParam param = new IndividualBeneficiaryParam();
        param.setCompanyId("100");
        param.setMaxDepth(1);
        param.setHistoryFlag(true);

        TraceResult traceResult = algo.evaluate(param);

        assertValidEndBeneficiaries(traceResult, "DEMO-INDIVIDUAL-2");
    }

    @Test
    public void testHistoryFlagFalse() {

        IndividualBeneficiaryParam param = new IndividualBeneficiaryParam();
        param.setCompanyId("100");
        param.setMaxDepth(1);
        param.setHistoryFlag(false);

        TraceResult traceResult = algo.evaluate(param);

        assertValidEndBeneficiaries(traceResult, "DEMO-INDIVIDUAL-1");
    }
}
