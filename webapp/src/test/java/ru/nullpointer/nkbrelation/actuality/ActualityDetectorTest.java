package ru.nullpointer.nkbrelation.actuality;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.OutdatedCondition;
import ru.nullpointer.nkbrelation.repo.GraphRepository;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: rainbow
 * Date: 04.12.15 15:19
 */

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/relations/DuplicateRelations.xml"
})
@Ignore
public class ActualityDetectorTest {

    private static final Logger logger = LoggerFactory.getLogger(ActualityDetectorTest.class);

    @Resource
    private GraphRepository graphRepository;

    @Test
    public void testActualityWithoutOutdatedCondition() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("FOUNDER_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, null);

        assertNotNull(result);
        assertEquals(result.getList().size(), 2);

        Map<String, Object> node = result.getList().get(0);
        assertEquals(node.get("id"), "Individual-1");

        List<Map<String, Object>> relations = (List<Map<String, Object>>) node.get("_relations");
        assertNotNull(relations);
        assertEquals(relations.size(), 8);
    }

    @Test
    public void testActualityForFounderCompany() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("FOUNDER_COMPANY");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, null);

        assertNotNull(result);
        assertEquals(result.getList().size(), 1);

        Map<String, Object> node = result.getList().get(0);
        assertEquals(node.get("nameshortsort"), "Company 1");

        List<Map<String, Object>> relations = (List<Map<String, Object>>) node.get("_relations");
        assertNotNull(relations);
        assertEquals(relations.size(), 3);

        Condition outdatedCondition = new OutdatedCondition(true);
        List<Condition> relConditions = new ArrayList<Condition>();
        relConditions.add(outdatedCondition);

        PaginatedQueryResult<Map<String, Object>> result2 = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), relConditions, pc, null);

        assertNotNull(result2);
        assertEquals(result2.getList().size(), 1);

        Map<String, Object> node2 = result2.getList().get(0);
        assertEquals(node.get("nameshortsort"), "Company 1");

        List<Map<String, Object>> relations2 = (List<Map<String, Object>>) node2.get("_relations");
        assertNotNull(relations2);
        assertEquals(relations2.size(), 3);
    }

    @Test
    public void testActualityForExecutiveIndividual() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("EXECUTIVE_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, null);

        assertNotNull(result);
        assertEquals(result.getList().size(), 2);

        Map<String, Object> node = result.getList().get(0);
        assertEquals(node.get("id"), "Individual-1");

        List<Map<String, Object>> relations = (List<Map<String, Object>>) node.get("_relations");
        assertNotNull(relations);
        assertEquals(relations.size(), 8);

        Condition outdatedCondition = new OutdatedCondition(true);
        List<Condition> relConditions = new ArrayList<Condition>();
        relConditions.add(outdatedCondition);

        PaginatedQueryResult<Map<String, Object>> result2 = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), relConditions, pc, null);

        assertNotNull(result2);
        assertEquals(result2.getList().size(), 2);

        Map<String, Object> node2 = result2.getList().get(0);
        assertEquals(node.get("id"), "Individual-1");

        List<Map<String, Object>> relations2 = (List<Map<String, Object>>) node2.get("_relations");
        assertNotNull(relations2);
        assertEquals(relations2.size(), 8);
    }

    @Test
    public void testActualityAbsence() {

        // атрибут outdated должен отсутстовать у связи типа ADDRESS и других

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

        assertTrue(!relations.get(0).containsKey("outdated"));
    }
}
