package ru.nullpointer.nkbrelation.api.rest.open.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.nullpointer.nkbrelation.api.rest.AbstractAPI;
import ru.nullpointer.nkbrelation.api.rest.id.ConditionUtils;
import ru.nullpointer.nkbrelation.api.rest.open.domain.Company;
import ru.nullpointer.nkbrelation.domain.PaginatedNodeResult;
import ru.nullpointer.nkbrelation.query.Condition;
import ru.nullpointer.nkbrelation.query.EqualsAnyCondition;
import ru.nullpointer.nkbrelation.service.GraphService;
import ru.nullpointer.nkbrelation.service.openapi.OpenAPIHelper;
import ru.nullpointer.nkbrelation.support.PageConfig;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;
import ru.nullpointer.nkbrelation.web.support.Pager;

/**
 * @author Maksim Konyuhov
 * @author ankostyuk
 */
@Controller
public class CompanyAPI extends AbstractAPI {

    @Resource
    private GraphService graphService;
    @Resource
    private OpenAPIHelper openAPIHelper;
    //

    @ResponseBody
    @RequestMapping(value = "/api/company", method = RequestMethod.GET)
    public ResponseEntity<?> findCompany(CompanyParam companyParam, Pager pager) {
        if (companyParam.isEmpty()) {
            return new ResponseEntity<Map<String, Object>>(Collections.<String, Object>emptyMap(), HttpStatus.BAD_REQUEST);
        }

        List<Condition> conditions = createConditions(companyParam);

        PaginatedNodeResult nodeResult = graphService.findNodes("COMPANY", conditions, pager.getPageConfig());

        return new ResponseEntity<PaginatedQueryResult<Company>>(convertResult(nodeResult), HttpStatus.OK);
    }

    private List<Condition> createConditions(CompanyParam c) {
        List<Condition> conditions = new ArrayList<Condition>();

        if (c.getId() != null) {
            conditions.add(EqualsAnyCondition.trueForOne("bsn_id", c.getId()));
        }

        if (c.getInn() != null) {
            conditions.add(EqualsAnyCondition.trueForOne("inn", c.getInn()));
        }

        if (c.getOgrn() != null) {
            conditions.add(EqualsAnyCondition.trueForOne("ogrn", c.getOgrn()));
        }

        if (c.getOkpo() != null) {
            conditions.add(EqualsAnyCondition.trueForOne("okpo", c.getOkpo()));
        }

        if (c.getName() != null) {
            conditions.add(EqualsAnyCondition.trueForOne("name", c.getName()));
        }

        if (!c.isSubsidiary()) {
            conditions.add(ConditionUtils.getSubsidiaryFilter());
        }

        return conditions;
    }

    private PaginatedQueryResult<Company> convertResult(PaginatedNodeResult nodeResult) {
        List<Map<String, Object>> list = nodeResult.getList();
        List<Company> companies = new ArrayList<Company>(list.size());

        for (Map<String, Object> node : list) {
            Company company = openAPIHelper.buildCompanyFromNode(node);
            companies.add(company);
        }

        return new PaginatedQueryResult<Company>(new PageConfig(nodeResult.getPageNumber(), nodeResult.getPageSize()), companies, nodeResult.getTotal());
    }
}
