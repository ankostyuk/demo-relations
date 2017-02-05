package ru.nullpointer.nkbrelation.actuality;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.OutdatedCondition;
import ru.nullpointer.nkbrelation.repo.GraphRepository;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;
import ru.nullpointer.nkbrelation.domain.PaginatedNodeResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: rainbow
 * Date: 04.02.16 17:52
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/relations/InnCounters.xml"
})
@Ignore
public class InnCounterTest {

    @Resource
    private GraphRepository graphRepository;

    @Test
    @Ignore
    public void testInnCounters() {

        String nodeId = "100";
        RelationType relType = graphRepository.getRelationType("FOUNDER_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        Condition outdatedCondition = new OutdatedCondition(true);
        List<Condition> relConditions = new ArrayList<Condition>();
        relConditions.add(outdatedCondition);

        PaginatedNodeResult result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), relConditions, pc, null);

        assertNotNull(result);
        assertNotNull(result.getInfo().getRelFacet());

        Map<String, Long> innMap = result.getInfo().getRelFacet().get("inn");
        assertTrue(innMap.get("77001") == 1);
        assertTrue(innMap.get("77002") == 1);
        assertTrue(innMap.get("77003") == 1);
    }

    @Test
    @Ignore
    public void testInnCountersHistoryTrue() {

        String nodeId = "100";
        RelationType relType = graphRepository.getRelationType("FOUNDER_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        Condition outdatedCondition = new OutdatedCondition(true);
        List<Condition> relConditions = new ArrayList<Condition>();
        relConditions.add(outdatedCondition);

        PaginatedNodeResult result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), relConditions, pc, true);

        assertNotNull(result);
        assertNotNull(result.getInfo().getRelFacet());

        Map<String, Long> innMap = result.getInfo().getRelFacet().get("inn");
        assertTrue(!innMap.containsKey("77001"));
        assertTrue(!innMap.containsKey("77002"));
        assertTrue(innMap.get("77003") == 1);
    }

    @Test
    @Ignore
    public void testInnCountersHistoryFalse() {

        String nodeId = "100";
        RelationType relType = graphRepository.getRelationType("FOUNDER_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        Condition outdatedCondition = new OutdatedCondition(true);
        List<Condition> relConditions = new ArrayList<Condition>();
        relConditions.add(outdatedCondition);

        PaginatedNodeResult result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), relConditions, pc, false);

        assertNotNull(result);
        assertNotNull(result.getInfo().getRelFacet());

        Map<String, Long> innMap = result.getInfo().getRelFacet().get("inn");
        assertTrue(innMap.get("77001") == 1);
        assertTrue(innMap.get("77002") == 1);
        assertTrue(!innMap.containsKey("77003"));
    }
}
