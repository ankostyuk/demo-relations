package ru.nullpointer.nkbrelation.api.rest.open.relations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import ru.nullpointer.nkbrelation.api.log.APILogEntry;
import ru.nullpointer.nkbrelation.api.rest.AbstractAPI;
import ru.nullpointer.nkbrelation.api.rest.open.domain.PublicNode;
import ru.nullpointer.nkbrelation.domain.*;
import ru.nullpointer.nkbrelation.service.openapi.OpenAPIService;
import ru.nullpointer.nkbrelation.support.PaginatedQueryResult;
import ru.nullpointer.nkbrelation.web.support.Pager;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

/**
 * @author ankostyuk
 */
@Controller
public class RelationsAPI extends AbstractAPI {

    private static Logger logger = LoggerFactory.getLogger(RelationsAPI.class);
    //
    @Resource
    private OpenAPIService openAPIService;
    //

    // /api/COMPANY/123/related/FOUNDER_COMPANY/in
    // /api/INDIVIDUAL/321/related/FOUNDER_INDIVIDUAL/out
    @ResponseBody
    @RequestMapping(value = "/api/{nodeType}/{id}/related/{relType}/{direction}", method = RequestMethod.GET)
    public ResponseEntity<?> getRelated(
            @PathVariable("nodeType") String nodeType,
            @PathVariable("id") String id,
            @PathVariable("relType") String relType,
            @PathVariable("direction") String direction,
            @RequestParam(value = "history", required = false) Boolean history,
            Pager pager,
            WebRequest request,
            APILogEntry entry) {

        Map<String, String[]> params = request.getParameterMap();
        NodeId nodeId = new NodeId(nodeType, id);

        PaginatedQueryResult<PublicNode> result = openAPIService.getRelated(nodeId, relType, direction, pager, params, history);

        if (result == null) {
            return new ResponseEntity<Map<String, Object>>(Collections.<String, Object>emptyMap(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<PaginatedQueryResult<PublicNode>>(result, HttpStatus.OK);
    }
}
