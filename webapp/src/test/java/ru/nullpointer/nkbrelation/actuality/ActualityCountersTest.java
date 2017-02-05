package ru.nullpointer.nkbrelation.actuality;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.domain.NodeInfo;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.OutdatedCondition;
import ru.nullpointer.nkbrelation.repo.GraphRepository;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * User: rainbow
 * Date: 08.12.15 21:26
 */

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/relations/DuplicateRelations.xml"
})
@Ignore
public class ActualityCountersTest {

    private static final Logger logger = LoggerFactory.getLogger(ActualityDetectorTest.class);

    @Resource
    private GraphRepository graphRepository;

    @Test
    public void testActualityCounters() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("FOUNDER_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        Condition outdatedCondition = new OutdatedCondition(true);
        List<Condition> relConditions = new ArrayList<Condition>();
        relConditions.add(outdatedCondition);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), relConditions, pc, null);

        assertNotNull(result);
        assertEquals(result.getList().size(), 2);

        Map<String, Object> node = result.getList().get(0);
        assertEquals(node.get("id"), "Individual-1");

        NodeInfo nodeInfo = (NodeInfo) node.get("_info");
        Map<String, Object> out = nodeInfo.getOut();
        assertEquals(out.size(), 2);

        assertTrue(out.get("EXECUTIVE_INDIVIDUAL") instanceof Map);

        Map<String, Integer> outMapEI = (Map<String, Integer>) out.get("EXECUTIVE_INDIVIDUAL");
        Map<String, Integer> outMapFI = (Map<String, Integer>) out.get("FOUNDER_INDIVIDUAL");

        assertTrue(outMapEI.get("actual").equals(2));
        assertNull(outMapEI.get("outdated"));
        assertTrue(outMapFI.get("actual").equals(4));
        assertTrue(outMapFI.get("outdated").equals(0));
    }

    @Test
    public void testNoActualityCounters() {

        String nodeId = "1";
        RelationType relType = graphRepository.getRelationType("ADDRESS");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, null);

        assertNotNull(result);
        assertEquals(result.getList().size(), 1);

        Map<String, Object> node = result.getList().get(0);
        assertEquals(node.get("id"), "DEMO-ADDRESS-1");

        List<Map<String, Object>> relations = (List<Map<String, Object>>) node.get("_relations");
        assertNotNull(relations);
        assertEquals(relations.size(), 1);

        NodeInfo nodeInfo = (NodeInfo) node.get("_info");
        Map<String, Object> out = nodeInfo.getOut();
        assertEquals(out.size(), 1);

        assertTrue(out.get("ADDRESS") instanceof Map);
        Map<String, Integer> outMap = (Map<String, Integer>) out.get("ADDRESS");
        assertTrue(outMap.get("actual").equals(1));
    }
}
