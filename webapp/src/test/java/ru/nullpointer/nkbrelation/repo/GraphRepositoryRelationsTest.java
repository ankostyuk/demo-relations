package ru.nullpointer.nkbrelation.repo;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.domain.meta.NodeType;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.SearchCondition;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;
import ru.nullpointer.nkbrelation.support.SortConfig;

import javax.annotation.Resource;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: rainbow
 * Date: 30.11.15
 * Time: 19:49
 */

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/relations/DuplicateRelations.xml"
})
public class GraphRepositoryRelationsTest {

    private static final Logger logger = LoggerFactory.getLogger(GraphRepositoryRelationsTest.class);

    @Resource
    private GraphRepository graphRepository;

    @Test
    @Ignore
    public void testDuplicates() {

        String okpo = "78486593";

        PageConfig pc = new PageConfig(1, 10);
        SortConfig sc = new SortConfig("aaa", true);

        SearchCondition search = new SearchCondition("q", okpo);
        NodeType type = graphRepository.getNodeType("COMPANY");

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.findNodes(type, Arrays.<Condition>asList(search), pc, sc);

        assertNotNull(result);
        assertEquals(result.getList().size(), 1);

        Map<String, Object> node = result.getList().get(0);
        assertEquals(node.get("bsn_id"), Long.valueOf(2));
        assertEquals(node.get("okpo"), okpo);

        List<Map<String, Object>> relations = (List<Map<String, Object>>) node.get("_relations");
        assertNotNull(relations);

        Set<Map<String, Object>> duplicateRelations = findDuplicateRelations(relations);
        assertNotNull(duplicateRelations);
        assertEquals(duplicateRelations.size(), 0);
        logger.debug("duplicate relations found: {}", duplicateRelations);

    }

    private Set<Map<String, Object>> findDuplicateRelations(List<Map<String, Object>> relations) {

        Set<Map<String, Object>> duplicateRelations = new HashSet<Map<String, Object>>();
        Set<Map<String, Object>> relationsSet = new HashSet<Map<String, Object>>();

        for(Map<String, Object> relation : relations) {

            if(!relationsSet.add(relation)) {

                duplicateRelations.add(relation);
            }
        }

        return duplicateRelations;
    }
}
