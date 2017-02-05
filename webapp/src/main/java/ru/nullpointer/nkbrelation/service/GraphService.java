package ru.nullpointer.nkbrelation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.nullpointer.nkbrelation.common.transform.PropertyTransformContext;
import ru.nullpointer.nkbrelation.domain.*;
import ru.nullpointer.nkbrelation.domain.meta.NodeType;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.EqualsAnyCondition;
import ru.nullpointer.nkbrelation.query.RelationTypeCondition;
import ru.nullpointer.nkbrelation.repo.GraphRepository;
import ru.nullpointer.nkbrelation.service.security.Permissions;
import ru.nullpointer.nkbrelation.service.security.SecurityService;
import ru.nullpointer.nkbrelation.service.transform.ContextComposite;
import ru.nullpointer.nkbrelation.service.transform.ContextPropertyTransformManager;
import ru.nullpointer.nkbrelation.service.transform.SimpleContext;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.SortConfig;

import javax.annotation.Resource;
import java.util.*;

import static ru.nullpointer.nkbrelation.actuality.ActualityDetector.*;
import static ru.nullpointer.nkbrelation.service.ValidationUtils.checkNotBlankValues;

/**
 *
 * @author Alexander Yastrebov
 * @author ankostyuk
 * @author Brok-Volchansky
 */
@Service
public class GraphService {

    private static final Logger logger = LoggerFactory.getLogger(GraphService.class);
    //
    @Resource
    private GraphRepository graphRepository;
    //
    @Resource
    private SecurityService securityService;
    //
    @Resource(name = "transformManagers")
    private List<ContextPropertyTransformManager> transformManagers;

    public List<NodeType> getNodeTypeList() {
        return graphRepository.getNodeTypeList();
    }

    public NodeType getNodeTypeByName(String name) {
        Assert.hasText(name);
        return graphRepository.getNodeType(name);
    }

    public List<RelationType> getRelationTypeList() {
        return graphRepository.getRelationTypeList();
    }

    public RelationType getRelationTypeByName(String name) {
        Assert.hasText(name);
        return graphRepository.getRelationType(name);
    }

    public PaginatedNodeResult findNodes(String nodeType, List<Condition> conditions, PageConfig pageConfig) {
        // Поиск доступен всем
        //securityService.ensureHasPermission(Permissions.SEARCH.name());

        Assert.notNull(conditions);
        Assert.notNull(pageConfig);

        NodeType type = getNodeTypeByName(nodeType);
        SortConfig sortConfig = new SortConfig(type.getSortField(), type.isSortAscending());

        PaginatedNodeResult result = graphRepository.findNodes(type, conditions, pageConfig, sortConfig);

        transformNodes(result.getList(), getResultContext("query"));

        return result;
    }

    public PaginatedNodeResult getRelatedNodes(//
            String nodeId,//
            String relTypeName,//
            boolean children,//
            List<Condition> nodeConditions,//
            List<Condition> relConditions,//
            PageConfig pageConfig,//
            Boolean historyFlag
    ) {
        Assert.notNull(pageConfig);

        securityService.ensureHasPermission(Permissions.SEARCH_RELATED.name());

        logger.debug("getRelatedNodes for {} {} {} {} {}", nodeId, relTypeName, children, nodeConditions, relConditions);

        boolean outdatedFlag = isOutdated(relConditions);

        RelationType relType = getRelationTypeByName(relTypeName);

        PaginatedNodeResult result = graphRepository.getRelatedNodes(nodeId, relType, children, nodeConditions, relConditions, pageConfig, historyFlag);
        if (result == null) {
            return new PaginatedNodeResult(pageConfig, Collections.EMPTY_LIST, 0);
        }
        transformNodes(result.getList(), getResultContext("related"));

        if (outdatedFlag) {
            return result;
        } else {
            List<Map<String, Object>> filteredList = filterNodesByActuality(result.getList(), nodeId, relTypeName, children);
            return new PaginatedNodeResult(new PageConfig(result.getPageNumber(), result.getPageSize()), filteredList, result.getTotal());
        }
    }

    private List<Map<String, Object>> filterNodesByActuality(List<Map<String, Object>> list, String nodeId, String relTypeName, boolean children) {

        List<Map<String, Object>> dstList = new ArrayList<Map<String, Object>>();
        boolean nodeOutdatedFlag;

        for (Map<String, Object> nodeMap : list) {

            nodeOutdatedFlag = true;

            List<Map <String, Object>> relations = (List<Map<String, Object>>) nodeMap.get("_relations");

            for (Map <String, Object> rel : relations) {

                Object otherNodeId = children ? rel.get("_srcId") : rel.get("_dstId");

                if (otherNodeId.equals(nodeId) && rel.get("_type").equals(relTypeName) &&
                        (rel.get("outdated") == null || !((Boolean)rel.get("outdated")))) {

                    nodeOutdatedFlag = false;
                    break;
                }
            }

            if (!nodeOutdatedFlag) {
                dstList.add(nodeMap);
            }
        }

        return dstList;
    }

    public GraphResult evaluateAlgorithm(String algoName, Object param) {
        GraphResult result = graphRepository.evaluateAlgorithm(algoName, param);

        transformNodes(result.getNodes(), getCurrentContext());

        return result;
    }

    public Object createParam(String algoName) {
        return graphRepository.createParam(algoName);
    }

    // internal api
    public PropertyTransformContext getCurrentContext() {
        PropertyTransformContext[] ctxs = new PropertyTransformContext[transformManagers.size()];
        int i = 0;
        for (ContextPropertyTransformManager m : transformManagers) {
            ctxs[i++] = m.getContext();
        }
        return new ContextComposite(ctxs);
    }

    // internal api
    // Порядок результата не гарантирован
    public List<Map<String, Object>> findNodes(List<NodeId> ids, PropertyTransformContext ctx) {
        Assert.notNull(ids);
        Assert.notNull(ctx);

        List<Map<String, Object>> nodes = graphRepository.findNodes(ids);

        transformNodes(nodes, ctx);

        return nodes;
    }

    // internal api
    public SingleNodeResult findNode(String nodeType, List<Condition> conditions, PropertyTransformContext ctx) {
        Assert.notNull(conditions);
        Assert.notNull(ctx);

        NodeType type = getNodeTypeByName(nodeType);
        PaginatedNodeResult result = graphRepository.findNodes(type, conditions, new PageConfig(1, 1), null);

        long total = result.getTotal();
        if (total == 0) {
            return SingleNodeResult.EMPTY;
        }
        transformNodes(result.getList(), ctx);

        return new SingleNodeResult(result.getList().get(0), total);
    }

    // internal api
    public StoreRelatedResponse storeRelatedNodes(String source, StoreRelatedRequest r, List<Condition> deleteConditions, boolean deleteAllRelationsFlag) {
        Assert.hasText(source);
        validate(r);

        logger.info("storeRelatedNodes: Start: source: {}, holder: {}, deleteConditions: {}",
                new Object[]{source, r.getHolder(), deleteConditions});

        //
        // Сначала сохраняем ноды, т.к. в них м.б. холдер
        //
        if (r.getNodes() != null && !r.getNodes().isEmpty()) {
            int stored = graphRepository.storeNodes(r.getNodes());

            logger.info("storeRelatedNodes: Stored {}/{} nodes", stored, r.getNodes().size());
        } else {
            logger.info("storeRelatedNodes: nodes are empty");
        }

        NodeId holderId = graphRepository.resolveNodeId(r.getHolder());
        if (holderId == null) {
            logger.warn("Holder not found: {}", r.getHolder());
            return null;
        }

        Set<NodeId> updatedNodes = updateRelations(source, holderId, r.getRelations(), deleteConditions, deleteAllRelationsFlag);

        logger.info("storeRelatedNodes: Finish: source: {}, holder: {}, deleteConditions: {}",
                new Object[]{source, r.getHolder(), deleteConditions});

        logger.info("storeRelatedNodes: Updated nodes: {}", updatedNodes);

        StoreRelatedResponse response = new StoreRelatedResponse();
        response.setHolder(holderId);
        response.setUpdated(updatedNodes);
        return response;
    }

    private void transformNodes(List<Map<String, Object>> nodes, PropertyTransformContext ctx) {
        for (ContextPropertyTransformManager m : transformManagers) {
            m.transformNodes(nodes, ctx);
        }
    }

    private Set<NodeId> updateRelations(String source, NodeId holderId,
            List<Map<String, Object>> relations, List<Condition> deleteConditions, boolean deleteAllRelationsFlag) {

        if (relations == null || relations.isEmpty()) {
            logger.info("updateRelations: relations are empty");
            return Collections.emptySet();
        }

        logger.debug("updateRelations: {} {}", relations.size(), relations);

        List<Condition> fullConditions = new ArrayList<Condition>();

        // при обновлении выписок по компаниям удаляем _все_ связи от ноды,
        // т.к. далее будут добавлены связи после обсчета (полный комплект, а не только те, что были добавлены в выписке)

        if(!deleteAllRelationsFlag) {
            fullConditions.add(EqualsAnyCondition.trueForOne("_source", source));
        }
        if (deleteConditions != null) {
            fullConditions.addAll(deleteConditions);
        }

        // TODO при переходе к связям 1 уровяя из монги убрать это условие

        fullConditions.add(new RelationTypeCondition(KEY_TYPE, Arrays.asList(RELATION_TYPE_FOUNDER_COMPANY, RELATION_TYPE_FOUNDER_INDIVIDUAL, RELATION_TYPE_EXECUTIVE_INDIVIDUAL), true));

        List<Map<String, Object>> deleted = graphRepository.deleteHolderRelations(holderId, fullConditions);
        if (deleted == null) {
            nodeNotFound(holderId.getType(), holderId.getId());
        }
        logger.debug("updateRelations: Deleted {}", deleted);
        logger.info("updateRelations: Deleted {} holding relations for node {}",
                new Object[]{deleted.size(), holderId});

        List<Map<String, Object>> stored = graphRepository.storeRelations(relations);

        logger.info("updateRelations: Stored {}/{} relations", stored.size(), relations.size());

        Set<NodeId> result = getNodeKeys(stored);
        result.addAll(getNodeKeys(deleted));

        return result;
    }

    private Set<NodeId> getNodeKeys(List<Map<String, Object>> relations) {
        Set<NodeId> result = new HashSet<NodeId>(relations.size());
        for (Map<String, Object> rel : relations) {
            String type = rel.get("_type").toString();
            String srcId = rel.get("_srcId").toString();
            String dstId = rel.get("_dstId").toString();

            RelationType relType = graphRepository.getRelationType(type);

            result.add(new NodeId(relType.getSourceNodeType(), srcId));
            result.add(new NodeId(relType.getDestinationNodeType(), dstId));
        }
        return result;
    }

    private void nodeNotFound(String type, String id) {
        throw new NotFoundException("Node of type '" + type + "' with id '" + id + "' not found");
    }

    private void validate(StoreRelatedRequest r) {
        Assert.notNull(r);
        Assert.notNull(r.getHolder());
        Assert.hasText(r.getHolder().getId());

        getNodeTypeByName(r.getHolder().getType());

        validateNodes(r.getNodes());
        validateRelations(r.getRelations());
    }

    private void validateNodes(List<Map<String, Object>> nodes) {
        if (nodes == null) {
            return;
        }
        for (Map<String, Object> node : nodes) {
            checkNotBlankValues(node, "type", "id");

            String t = node.get("type").toString();
            // проверить тип
            getNodeTypeByName(t);
        }
    }

    private void validateRelations(List<Map<String, Object>> relations) {
        if (relations == null) {
            return;
        }
        for (Map<String, Object> rel : relations) {
            checkNotBlankValues(rel, "_type", "_srcId", "_dstId");
        }
    }

    private PropertyTransformContext getResultContext(String type) {
        return new ContextComposite(getCurrentContext(), new SimpleContext("resultType", type));
    }

    private boolean isOutdated(List<Condition> conditions) {
        return true;
    }
}
