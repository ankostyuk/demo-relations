package ru.nullpointer.nkbrelation.api.rest;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.nullpointer.nkbrelation.api.log.APILogEntry;
import ru.nullpointer.nkbrelation.service.billing.BillingService;

/**
 *
 * @author ankostyuk
 */
@Controller
public class ProductAPI extends AbstractAPI {

    private static final String QUERY_PARAMS_KEY = "query";
    //
    @Resource
    private BillingService billingService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/api/product/{productCode}/purchase", method = RequestMethod.POST)
    public void purchaseProduct(
            @PathVariable("productCode") String productCode,
            @RequestBody Map<String, String> queryParams, APILogEntry entry
    ) {
        Map<String, Object> params = Collections.<String, Object>singletonMap(QUERY_PARAMS_KEY, queryParams);

        billingService.requestAuthenticatedProduct(productCode, params);

        logger.info("{}, product: {}, params: {}", entry, productCode, params);
    }
}
