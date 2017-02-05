package ru.nullpointer.nkbrelation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nullpointer.nkbrelation.api.rest.query.QueryParser;
import ru.nullpointer.nkbrelation.domain.PaginatedNodeResult;
import ru.nullpointer.nkbrelation.domain.meta.NodeType;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.SearchCondition;
import ru.nullpointer.nkbrelation.web.support.Pager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ankostyuk
 */
@Service
public class APIService {

    private static Logger logger = LoggerFactory.getLogger(APIService.class);
    //
    private final QueryParser queryParser = new QueryParser();
    //
    @Resource
    private GraphService graphService;
    //

    public PaginatedNodeResult getRelatedNodes(
            String nodeId,
            String relTypeName,
            boolean children,
            Pager pager,
            Map<String, String[]> params,
            Boolean history) {

        RelationType relType = graphService.getRelationTypeByName(relTypeName);

        NodeType relatedNodeType = graphService.getNodeTypeByName(relType.getNodeType(!children));

        SearchCondition search = queryParser.parseSearch(relatedNodeType.getSearchTypes(), params);
        List<Condition> nodeConditions = queryParser.parseFilters("node", relatedNodeType.getProperties(), params);
        List<Condition> relConditions = queryParser.parseFilters("rel", relType.getProperties(), params);

        PaginatedNodeResult result = graphService.getRelatedNodes(nodeId, relTypeName, children, mergeConditions(nodeConditions, search), relConditions, pager.getPageConfig(), history);

        return result;
    }

    public List<Condition> mergeConditions(List<Condition> conditions, SearchCondition search) {
        if (search == null) {
            return conditions;
        }
        List<Condition> result = new ArrayList<Condition>(conditions.size() + 1);
        result.addAll(conditions);
        result.add(search);
        return result;
    }
}
