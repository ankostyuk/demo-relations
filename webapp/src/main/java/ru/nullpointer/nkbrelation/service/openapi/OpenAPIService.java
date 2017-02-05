package ru.nullpointer.nkbrelation.service.openapi;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nullpointer.nkbrelation.api.rest.open.domain.PublicNode;
import ru.nullpointer.nkbrelation.api.rest.query.QueryParser;
import ru.nullpointer.nkbrelation.domain.NodeId;
import ru.nullpointer.nkbrelation.domain.PaginatedNodeResult;
import ru.nullpointer.nkbrelation.service.APIContext;
import ru.nullpointer.nkbrelation.service.APIService;
import ru.nullpointer.nkbrelation.service.GraphService;
import ru.nullpointer.nkbrelation.support.DebugUtils;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;
import ru.nullpointer.nkbrelation.web.support.Pager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * @author ankostyuk
 */
@Service
public class OpenAPIService {

    private static Logger logger = LoggerFactory.getLogger(OpenAPIService.class);
    //
    private final QueryParser queryParser = new QueryParser();
    //
    @Resource
    private GraphService graphService;
    @Resource
    private APIService apiService;
    @Resource
    private OpenAPIHelper openAPIHelper;
    //

    @SuppressWarnings("unchecked")
    public PaginatedQueryResult<PublicNode> getRelated(
            NodeId nodeId,
            String relTypeName,
            String direction,
            Pager pager,
            Map<String, String[]> params,
            Boolean history) {

        List<Map<String, Object>> nodes = graphService.findNodes(asList(nodeId), APIContext.INSTANCE);

        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }

        Map<String, Object> node = nodes.get(0);

        boolean children = openAPIHelper.directionToChildren(direction);

        PaginatedNodeResult relatedNodesResult = apiService.getRelatedNodes(nodeId.getId(), relTypeName, children, pager, params, history);

        return fillRelatedResult(node, relatedNodesResult, relTypeName, children);
    }

    @SuppressWarnings("unchecked")
    private PaginatedQueryResult<PublicNode> fillRelatedResult(Map<String, Object> node, PaginatedNodeResult relatedNodesResult, String relTypeName, boolean children) {
        DebugUtils.debugNode(node, true);
        DebugUtils.debugMapList(relatedNodesResult.getList(), "related nodes", "node");

        List<Map<String, Object>> relations = (List<Map<String, Object>>) node.get("_relations");

        if (CollectionUtils.isEmpty(relations)) {
            return null;
        }

        //
        String targetNodeId = (String) node.get("_id");
        String targetNodeIdField = children ? "_srcId" : "_dstId";
        String relatedNodeIdField = children ? "_dstId" : "_srcId";

        Map<?, ?> collectedRelations  = relations.stream()
            .filter(r -> r.get("_type").equals(relTypeName) && r.get(targetNodeIdField).equals(targetNodeId))
            .collect(Collectors.groupingBy(r -> r.get(relatedNodeIdField)));

        //
        List<Map<String, Object>> relatedNodes = relatedNodesResult.getList();
        List<PublicNode> publicNodes = new ArrayList<PublicNode>(relatedNodes.size());

        for (Map<String, Object> relatedNode : relatedNodes) {
            List<Map<String, Object>> relatedNodeRelations = (List<Map<String, Object>>) collectedRelations.get(relatedNode.get("_id"));
            openAPIHelper.transformToPublicRelations(relatedNodeRelations);

            PublicNode publicNode = openAPIHelper.buildPublicNode(relatedNode);
            publicNode.setRelations(relatedNodeRelations);

            publicNodes.add(publicNode);
        }

        return new PaginatedQueryResult<PublicNode>(
            new PageConfig(relatedNodesResult.getPageNumber(), relatedNodesResult.getPageSize()),
            publicNodes,
            relatedNodesResult.getTotal()
        );
    }
}
