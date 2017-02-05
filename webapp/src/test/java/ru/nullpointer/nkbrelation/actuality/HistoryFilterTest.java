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
 * Date: 02.02.16 18:18
 */

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/relations/DuplicateRelations.xml"
})
@Ignore
public class HistoryFilterTest {

    private static final Logger logger = LoggerFactory.getLogger(HistoryFilterTest.class);

    @Resource
    private GraphRepository graphRepository;

    @Test
    public void testHistoryFlagTrue1() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("FOUNDER_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, true);

        assertNotNull(result);
        assertEquals(result.getList().size(), 0);
    }

    @Test
    public void testSetHistoryFlagFalse1() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("FOUNDER_INDIVIDUAL");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, false);

        assertNotNull(result);
        assertEquals(result.getList().size(), 2);

        Map<String, Object> node = result.getList().get(0);
        assertEquals(node.get("id"), "Individual-1");

        NodeInfo nodeInfo = (NodeInfo) node.get("_info");
        assertEquals(nodeInfo.getIn().size(), 0);
        assertEquals(nodeInfo.getOut().size(), 2);
        assertEquals(((Map<String, Object>) nodeInfo.getOut().get("FOUNDER_INDIVIDUAL")).size(), 2);
    }

    @Test
    public void testHistoryFlagTrue2() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("FOUNDER_COMPANY");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, true);

        assertNotNull(result);
        assertEquals(result.getList().size(), 1);
    }

    @Test
    public void testHistoryFlagFalse2() {

        String nodeId = "1000";
        RelationType relType = graphRepository.getRelationType("FOUNDER_COMPANY");
        PageConfig pc = new PageConfig(1, 10);

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.getRelatedNodes(nodeId, relType, false,
                new ArrayList<Condition>(), new ArrayList<Condition>(), pc, false);

        assertNotNull(result);
        assertEquals(result.getList().size(), 0);
    }
}
