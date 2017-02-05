package ru.nullpointer.nkbrelation.dao.algo;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.domain.TraceResult;

import javax.annotation.Resource;

import static ru.nullpointer.nkbrelation.dao.AlgoTestUtils.assertValidEndBeneficiaries;
import static ru.nullpointer.nkbrelation.dao.AlgoTestUtils.assertValidTraces;

/**
 *
 * @author nikolka
 * @author Alexander Yastrebov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/algo/IndividualBeneficiaryAlgo.xml"
})
public class IndividualBeneficiaryAlgoIT {

    private final Logger logger = LoggerFactory.getLogger(IndividualBeneficiaryAlgoIT.class);

    @Resource
    private IndividualBeneficiaryAlgo algo;

    @Test
    @Ignore
    public void testFirstLevel() {
        IndividualBeneficiaryParam param = new IndividualBeneficiaryParam();
        param.setCompanyId("100");
        param.setMaxDepth(1);

        TraceResult traceResult = algo.evaluate(param);

        assertValidEndBeneficiaries(traceResult, "DEMO-INDIVIDUAL-1", "DEMO-INDIVIDUAL-2", "DEMO-INDIVIDUAL-3");
        assertValidTraces(traceResult, 2);
    }

    @Test
    @Ignore
    public void testSecondLevel() {
        IndividualBeneficiaryParam param = new IndividualBeneficiaryParam();
        param.setCompanyId("100");
        param.setMaxDepth(2);

        TraceResult traceResult = algo.evaluate(param);

        assertValidEndBeneficiaries(traceResult, "DEMO-INDIVIDUAL-1", "DEMO-INDIVIDUAL-2", "DEMO-INDIVIDUAL-3", "DEMO-INDIVIDUAL-3");
        assertValidTraces(traceResult, 3);
    }

    @Test
    @Ignore
    public void testAllLevels() {
        IndividualBeneficiaryParam param = new IndividualBeneficiaryParam();
        param.setCompanyId("100");

        TraceResult traceResult = algo.evaluate(param);

        assertValidEndBeneficiaries(traceResult, "DEMO-INDIVIDUAL-1", "DEMO-INDIVIDUAL-2", "DEMO-INDIVIDUAL-3", "DEMO-INDIVIDUAL-3", "DEMO-INDIVIDUAL-4", "DEMO-INDIVIDUAL-5", "DEMO-INDIVIDUAL-6");
        assertValidTraces(traceResult, null);
    }
}
