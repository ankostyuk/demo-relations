package ru.nullpointer.nkbrelation.dao.algo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.kernel.impl.traversal.MonoDirectionalTraversalDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import ru.nullpointer.nkbrelation.common.NeoIdUtils;
import ru.nullpointer.nkbrelation.dao.DaoException;
import ru.nullpointer.nkbrelation.dao.GraphAlgorithm;
import ru.nullpointer.nkbrelation.dao.NodeDAO;
import ru.nullpointer.nkbrelation.dao.RelationTypeDAO;
import ru.nullpointer.nkbrelation.dao.impl.NeoUtils;
import ru.nullpointer.nkbrelation.dao.impl.NodeFactory;
import ru.nullpointer.nkbrelation.dao.impl.PathExpanderImpl;
import ru.nullpointer.nkbrelation.dao.impl.TraceResultBuilder;
import ru.nullpointer.nkbrelation.dao.impl.filter.RelationFilter;
import ru.nullpointer.nkbrelation.domain.TraceResult;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.EqualsAnyCondition;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Maksim Konyuhov
 * @author ankostyuk
 */
// TODO Обобщить код с IndividualBeneficiaryAlgo
public class FindKinsmenAlgo implements GraphAlgorithm<FindKinsmenParam, TraceResult>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(FindKinsmenAlgo.class);
    //
    private final static int MAX_DEPTH = 10;
    private final static int DEFAULT_DEPTH = 10;
    private final static int BATCH_SIZE = 1000;
    //
    private NodeFactory nodeFactory;
    private RelationTypeDAO relationTypeDAO;
    //
    private RelationFilter[] relationFilters;
    //
    private NodeDAO individualNodeDAO;

    @Resource
    private TraceResultBuilder traceResultBuilder;
    @Qualifier("graphDatabaseService")
    @Resource
    private GraphDatabaseService dbService;

    @Override
    public TraceResult evaluate(FindKinsmenParam param) {

        try (Transaction ignored = dbService.beginTx()) {

            String id = param.getIndividualId();
            Assert.hasText(id);

            Integer maxDepth = getMaxDepth(param);

            String individualId = NeoIdUtils.compoundId("INDIVIDUAL", id);
            Node node = nodeFactory.getNode(individualId);
            if (node == null) {
                throw new DaoException("Individual with id " + individualId + " not found");
            }

            PathExpanderImpl expander = new PathExpanderImpl();

            if (relationFilters != null) {
                for (RelationFilter filter : relationFilters) {
                    expander.addFilter(filter);
                }
            }

            TraversalDescription td = new MonoDirectionalTraversalDescription() //
                    .uniqueness(Uniqueness.NODE_GLOBAL)//
                    .breadthFirst()
                    .expand(expander);

            td = td.evaluator(new FindKinsmenEvaluator(maxDepth));

            logger.debug("Start traverse: {}", individualId);

            Traverser t = td.traverse(node);

            List<Path> paths = retainKinsmenPaths(id, t);

            Collections.sort(paths, PathComparator.INSTANCE);

            return traceResultBuilder.build(paths, param.getHistoryFlag(), relationTypeDAO);
        }
    }

    private int getMaxDepth(FindKinsmenParam param) {
        Integer maxDepth = param.getMaxDepth();
        if (maxDepth == null) {
            return DEFAULT_DEPTH;
        }
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be positive");
        }
        return Math.min(maxDepth, MAX_DEPTH);
    }

    private List<Path> retainKinsmenPaths(String individualId, Traverser t) {
        Iterator<Path> pit = t.iterator();
        if (!pit.hasNext()) {
            return Collections.emptyList();
        }
        String name = getName(individualId);
        logger.debug("Name: {}", name);

        List<Condition> conditions = createLastNameCondition(name);

        List<Path> result = new ArrayList<Path>();
        List<Path> batch = new ArrayList<Path>(BATCH_SIZE);
        int total = 0;
        int count = 0;
        while (pit.hasNext()) {
            batch.add(pit.next());
            total++;
            if (total % BATCH_SIZE == 0 || !pit.hasNext()) {
                count++;
                int input = batch.size();

                retainKinsmenPaths(conditions, batch);

                logger.debug("Batch#: {}, input: {}, result: {}", count, input, batch.size());
                if (!batch.isEmpty()) {
                    result.addAll(batch);
                    batch.clear();
                }
            }
        }
        logger.debug("Path count: {}, total checked: {}", result.size(), total);

        return result;
    }

    private void retainKinsmenPaths(List<Condition> conditions, List<Path> paths) {
        List<String> endIds = new ArrayList<String>(paths.size());
        for (Path path : paths) {
            endIds.add(NeoUtils.getIdValue(path.endNode()));
        }

        List<String> filteredIds = individualNodeDAO.filterIds(endIds, conditions, null).getList();

        if (filteredIds.isEmpty()) {
            paths.clear();
            return;
        }

        Set<String> ids = new HashSet<String>(filteredIds);
        Iterator<Path> it = paths.iterator();
        while (it.hasNext()) {
            Path path = it.next();
            String id = NeoUtils.getIdValue(path.endNode());
            if (!ids.contains(id)) {
                it.remove();
            }
        }
    }

    private String getName(String id) {
        List<Map<String, Object>> list = individualNodeDAO.findByIds(Collections.singletonList(id));
        return (String) list.get(0).get("name");
    }

    private List<Condition> createLastNameCondition(String fullName) {
        String query = "LASTNAME " + fullName;
        Condition condition = EqualsAnyCondition.trueForOne("namebase", query);
        return Collections.singletonList(condition);
    }

    @Override
    public FindKinsmenParam createParam() {
        return new FindKinsmenParam();
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

    public void setIndividualNodeDAO(NodeDAO individualNodeDAO) {
        this.individualNodeDAO = individualNodeDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(nodeFactory);
        Assert.notNull(relationTypeDAO);
        Assert.notNull(individualNodeDAO);
    }
}
