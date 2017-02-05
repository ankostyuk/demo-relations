package ru.nullpointer.nkbrelation.repo;

import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.support.FacetConfig;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;
import ru.nullpointer.nkbrelation.support.SortConfig;

/**
 *
 * @author Alexander Yastrebov
 */
@SuppressWarnings("unchecked")
@Ignore
public class GraphRepositoryFindNodesTest extends AbstractGraphRepositoryTest {

    @Test
    @Ignore
    public void noConditions() {
        PageConfig pc = mock(PageConfig.class);
        SortConfig sc = mock(SortConfig.class);

        when(companyNodeDAO.find(
                isNull(List.class),
                eq(Collections.<Condition>emptyList()),
                eq(pc),
                eq(sc),
                any(FacetConfig.class)
        ))//
                .thenReturn(nodeResult(pc, 100,
                                node("1", "COMPANY"),
                                node("2", "COMPANY"),
                                node("3", "COMPANY")));
        when(relationDAO.getRelations(companyNodeType, "1", true, false))//
                .thenReturn(actual(rel("FOUNDER_COMPANY", "2", "1")));
        when(relationDAO.getRelations(companyNodeType, "2", true, false))//
                .thenReturn(actual(rel("FOUNDER_COMPANY", "3", "2")));
        when(relationDAO.getRelations(companyNodeType, "3", true, false))//
                .thenReturn(actual(rel("FOUNDER_COMPANY", "1", "3")));

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.findNodes(companyNodeType, Collections.<Condition>emptyList(), pc, sc);

        assertNotNull(result);
        assertEquals(3, result.getList().size());
        for (Map<String, Object> n : result.getList()) {
            List<Map<String, Object>> rels = (List<Map<String, Object>>) n.get("_relations");
            assertNotNull(rels);
            assertFalse(rels.isEmpty());
        }
    }

    @Test
    @Ignore
    public void withConditions() {
        PageConfig pc = mock(PageConfig.class);
        SortConfig sc = mock(SortConfig.class);
        List<Condition> conditions = new ArrayList<Condition>();

        when(companyNodeDAO.find(
                isNull(List.class),
                eq(conditions),
                eq(pc),
                eq(sc),
                any(FacetConfig.class)
        ))//
                .thenReturn(nodeResult(pc, 100,
                                node("1", "COMPANY"),
                                node("2", "COMPANY"),
                                node("3", "COMPANY")));
        when(relationDAO.getRelations(companyNodeType, "1", true, false))//
                .thenReturn(actual(rel("FOUNDER_COMPANY", "2", "1")));
        when(relationDAO.getRelations(companyNodeType, "2", true, false))//
                .thenReturn(actual(rel("FOUNDER_COMPANY", "3", "2")));
        when(relationDAO.getRelations(companyNodeType, "3", true, false))//
                .thenReturn(actual(rel("FOUNDER_COMPANY", "1", "3")));

        PaginatedQueryResult<Map<String, Object>> result = graphRepository.findNodes(companyNodeType, conditions, pc, sc);

        assertNotNull(result);
        assertEquals(3, result.getList().size());
        for (Map<String, Object> n : result.getList()) {
            List<Map<String, Object>> rels = (List<Map<String, Object>>) n.get("_relations");
            assertNotNull(rels);
            assertFalse(rels.isEmpty());
        }
    }
}
