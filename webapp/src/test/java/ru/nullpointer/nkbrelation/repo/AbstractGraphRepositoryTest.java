package ru.nullpointer.nkbrelation.repo;

import java.util.*;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.nullpointer.nkbrelation.actuality.ActualityDetector;
import ru.nullpointer.nkbrelation.dao.NodeDAO;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import ru.nullpointer.nkbrelation.dao.RelationDAO;
import ru.nullpointer.nkbrelation.dao.impl.RelationCounts;
import ru.nullpointer.nkbrelation.domain.PaginatedNodeResult;
import ru.nullpointer.nkbrelation.domain.Relations;
import ru.nullpointer.nkbrelation.domain.meta.NodeType;
import ru.nullpointer.nkbrelation.repo.impl.GraphRepositoryImpl;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;

/**
 *
 * @author Alexander Yastrebov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/spring/meta.xml",
    "classpath:/spring/daoMocks.xml"
})
public abstract class AbstractGraphRepositoryTest { // NOPMD

    protected static final Logger logger = LoggerFactory.getLogger(AbstractGraphRepositoryTest.class); // NOPMD
    //
    @Resource
    private ApplicationContext applicationContext;
    //
    @Resource(name = "companyNodeType")
    protected NodeType companyNodeType;
    @Resource(name = "companyNodeDAO")
    protected NodeDAO companyNodeDAO;
    //
    @Resource(name = "individualNodeType")
    protected NodeType individualNodeType;
    @Resource(name = "individualNodeDAO")
    protected NodeDAO individualNodeDAO;
    //
    @Resource(name = "addressNodeType")
    protected NodeType addressNodeType;
    @Resource(name = "addressNodeDAO")
    protected NodeDAO addressNodeDAO;
    //
    @Resource(name = "phoneNodeType")
    protected NodeType phoneNodeType;
    @Resource(name = "phoneNodeDAO")
    protected NodeDAO phoneNodeDAO;
    //
    @Resource(name = "purchaseNodeType")
    protected NodeType purchaseNodeType;
    @Resource(name = "purchaseNodeDAO")
    protected NodeDAO purchaseNodeDAO;
    @Resource
    protected RelationDAO relationDAO;
    @Resource
    protected ActualityDetector actualityDetector;
    //
    protected GraphRepository graphRepository;

    @Before
    public void setUp() {
        reset(companyNodeDAO);
        reset(individualNodeDAO);
        reset(addressNodeDAO);
        reset(phoneNodeDAO);
        reset(purchaseNodeDAO);
        reset(relationDAO);

        when(companyNodeDAO.getNodeType()).thenReturn(companyNodeType);
        when(individualNodeDAO.getNodeType()).thenReturn(individualNodeType);
        when(addressNodeDAO.getNodeType()).thenReturn(addressNodeType);
        when(phoneNodeDAO.getNodeType()).thenReturn(phoneNodeType);
        when(purchaseNodeDAO.getNodeType()).thenReturn(purchaseNodeType);

        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

        graphRepository = beanFactory.createBean(GraphRepositoryImpl.class);
    }

    protected Map<String, Object> node(String id, String type) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("_id", id);
        result.put("_type", type);
        return result;
    }

    protected Map<String, Object> rel(String type, String srcId, String dstId) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("_srcId", srcId);
        result.put("_dstId", dstId);
        result.put("_type", type);
        return result;
    }

    protected Relations actual(Map<String, Object>... rels) {
        Relations r = new Relations();
        r.setActual(Arrays.asList(rels));
        return r;
    }

    protected PaginatedQueryResult<Map<String, Object>> paginated(PageConfig pc, long total, Map<String, Object>... nodes) {
        List<Map<String, Object>> list = Arrays.asList(nodes);
        PaginatedQueryResult<Map<String, Object>> result = new PaginatedQueryResult<Map<String, Object>>(pc, list, total);
        return result;
    }

    protected PaginatedNodeResult nodeResult(PageConfig pc, long total, Map<String, Object>... nodes) {
        List<Map<String, Object>> list = Arrays.asList(nodes);
        PaginatedNodeResult result = new PaginatedNodeResult(pc, list, total);
        return result;
    }
}
