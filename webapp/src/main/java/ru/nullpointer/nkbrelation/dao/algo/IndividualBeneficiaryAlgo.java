package ru.nullpointer.nkbrelation.dao.algo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.kernel.impl.traversal.MonoDirectionalTraversalDescription;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import ru.nullpointer.nkbrelation.actuality.ActualityDetector;
import ru.nullpointer.nkbrelation.common.NeoIdUtils;
import ru.nullpointer.nkbrelation.dao.DaoException;
import ru.nullpointer.nkbrelation.dao.GraphAlgorithm;
import ru.nullpointer.nkbrelation.dao.RelationTypeDAO;
import ru.nullpointer.nkbrelation.dao.impl.NodeFactory;
import ru.nullpointer.nkbrelation.dao.impl.PathExpanderImpl;
import ru.nullpointer.nkbrelation.dao.impl.TraceResultBuilder;
import ru.nullpointer.nkbrelation.dao.impl.filter.LoopRelationFilter;
import ru.nullpointer.nkbrelation.dao.impl.filter.RelationFilter;
import ru.nullpointer.nkbrelation.domain.TraceResult;

import javax.annotation.Resource;

/**
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
//TODO Обобщить код с FindKinsmenAlgo
public class IndividualBeneficiaryAlgo implements GraphAlgorithm<IndividualBeneficiaryParam, TraceResult>, InitializingBean {

    private NodeFactory nodeFactory;
    private RelationTypeDAO relationTypeDAO;
    //
    private RelationFilter[] relationFilters;

    @Resource
    private TraceResultBuilder traceResultBuilder;
    @Qualifier("graphDatabaseService")
    @Resource
    private GraphDatabaseService dbService;

    @Override
    public TraceResult evaluate(IndividualBeneficiaryParam param) {

        try (Transaction ignored = dbService.beginTx()) {

            String id = param.getCompanyId();
            Assert.hasText(id);

            Integer maxDepth = param.getMaxDepth();
            if (maxDepth != null && maxDepth <= 0) {
                throw new IllegalArgumentException("maxDepth must be positive");
            }

            String companyId = NeoIdUtils.compoundId("COMPANY", id);
            Node node = nodeFactory.getNode(companyId);
            if (node == null) {
                throw new DaoException("Company with id " + companyId + " not found");
            }

            PathExpanderImpl expander = new PathExpanderImpl();
            expander.setOutdatedFlag(param.getOutdated());
            expander.setDirection(IndividualBeneficiaryEvaluator.EXPAND_DIRECTION);
            for (RelationshipType t : IndividualBeneficiaryEvaluator.EXPAND_TYPES) {
                expander.addType(t);
            }
            expander.addFilter(LoopRelationFilter.INSTANCE);

            if (relationFilters != null) {
                for (RelationFilter filter : relationFilters) {
                    expander.addFilter(filter);
                }
            }

            TraversalDescription td = new MonoDirectionalTraversalDescription()//
                    //
                    // Между двумя нодами может быть несколько связей одного типа из
                    // разных источников. При фильтрации по свойству связи
                    // необходимо посмотреть _все_ связи, т.к. у одной может быть
                    // присутствовать фильтруемое свойство, а у другой - нет
                    // По умолчанию TraversalDescriptionImpl использует
                    // Uniqueness.NODE_GLOBAL
                    //
                    .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)//
                    .expand(expander);

            td = td.evaluator(new IndividualBeneficiaryEvaluator(maxDepth));
            td = td.sort(PathComparator.INSTANCE);

            Traverser t = td.traverse(node);

            return traceResultBuilder.build(t, param.getHistoryFlag(), relationTypeDAO);
        }
    }

    @Override
    public IndividualBeneficiaryParam createParam() {
        return new IndividualBeneficiaryParam();
    }

    public void setNodeFactory(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public void setRelationTypeDAO(RelationTypeDAO relationTypeDAO) {
        this.relationTypeDAO = relationTypeDAO;
    }

    public void setRelationFilters(RelationFilter[] relationFilters) {
        this.relationFilters = relationFilters;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(nodeFactory);
        Assert.notNull(relationTypeDAO);
    }
}
