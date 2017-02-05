package ru.nullpointer.nkbrelation.service.billing;

import javax.annotation.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import ru.nullpointer.nkbrelation.service.ServiceException;
import ru.nullpointer.nkbrelation.service.security.SecurityService;

/**
 *
 * @author ankostyuk
 */
public class SimpleBillingService implements BillingService {

    private static Logger logger = LoggerFactory.getLogger(SimpleBillingService.class);
    //
    @Resource
    private SecurityService securityService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void requestAuthenticatedOperation(String productCode, Object operationParams) {
        logger.debug("requestAuthenticatedOperation...");
        logger.debug("productCode: {}", productCode);
        logger.debug("operationParams: {}", operationParams);

        Authentication auth = getAuthentication();
        logger.debug("auth: {}", auth);

        try {
            String params = objectMapper.writeValueAsString(operationParams);
            logger.debug("params: {}", params);
        } catch (Exception e) { // NOPMD
            throw new ServiceException(e.getMessage(), e);
        }

        logger.debug("... success");
    }

    @Override
    public void requestAuthenticatedProduct(String productCode, Object productParams) {
        logger.debug("requestAuthenticatedProduct...");
        logger.debug("productCode: {}", productCode);
        logger.debug("productParams: {}", productParams);

        Authentication auth = getAuthentication();
        logger.debug("auth: {}", auth);

        try {
            String params = objectMapper.writeValueAsString(productParams);
            logger.debug("params: {}", params);
        } catch (Exception e) { // NOPMD
            throw new ServiceException(e.getMessage(), e);
        }

        logger.debug("... success");
    }

    private Authentication getAuthentication() {
        Authentication auth = securityService.getAuthentication();
        if (auth == null) {
            securityService.accessDenied("Not authenticated");
        }
        return auth;
    }
}
