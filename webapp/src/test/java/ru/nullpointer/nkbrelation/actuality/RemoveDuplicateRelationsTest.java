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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: rainbow
 * Date: 01.02.16 21:29
 */

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/relations/DuplicateRelations.xml"
})
@Ignore
public class RemoveDuplicateRelationsTest {

    private static final Logger logger = LoggerFactory.getLogger(RemoveDuplicateRelationsTest.class);

    @Resource
    private GraphRepository graphRepository;

    @Test
    public void testRemoveDuplicateRelations() {

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

        NodeInfo nodeInfo = (NodeInfo) node.get("_info");
        assertEquals(nodeInfo.getIn().size(), 0);
        assertEquals(nodeInfo.getOut().size(), 2);
        assertEquals(((Map<String, Object>) nodeInfo.getOut().get("FOUNDER_INDIVIDUAL")).size(), 2);

        Map<String, Object> node2 = result.getList().get(1);
        assertEquals(node2.get("id"), "Individual-2");

        List<Map<String, Object>> relations2 = (List<Map<String, Object>>) node2.get("_relations");
        assertNotNull(relations2);
        assertEquals(relations2.size(), 7);

        NodeInfo nodeInfo2 = (NodeInfo) node2.get("_info");
        assertEquals(nodeInfo2.getIn().size(), 0);
        assertEquals(nodeInfo2.getOut().size(), 2);
        assertEquals(((Map<String, Object>) nodeInfo2.getOut().get("FOUNDER_INDIVIDUAL")).size(), 2);
    }
}
