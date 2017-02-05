package ru.nullpointer.nkbrelation.service.billing;

import creditnet.cnas.auth.SsoTicket;
import creditnet.cnas.auth.exception.AuthException;
import creditnet.cnas.auth.exception.InsufficientBalanceException;
import creditnet.cnas.auth.exception.ProductLimitExceededException;
import creditnet.cnas.service.ClientRequest;
import creditnet.cnas.service.ClientRequestServiceEndpoint;
import creditnet.cnas.service.Product;
import creditnet.cnas.service.PurchasePossibility;
import java.util.Date;
import javax.annotation.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import ru.nullpointer.nkbrelation.service.NotFoundException;
import ru.nullpointer.nkbrelation.service.TimeService;
import ru.nullpointer.nkbrelation.service.security.SecurityService;
import ru.nullpointer.nkbrelation.service.security.TicketPrincipal;

/**
 *
 * @author ankostyuk
 * @author Alexander Yastrebov
 */
public class CnasBillingService implements BillingService, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(CnasBillingService.class);
    //
    private ClientRequestServiceEndpoint clientRequestServiceEndpoint;
    //
    @Resource
    private SecurityService securityService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private TimeService timeService;
    //

    @Override
    public void requestAuthenticatedOperation(String productCode, Object operationParams) {
        logger.debug("productCode: {}", productCode);
        logger.debug("operationParams: {}", operationParams);

        SsoTicket ticket = getSsoTicket();

        try {
            String params = objectMapper.writeValueAsString(operationParams);
            clientRequestServiceEndpoint.requestOperation(
                    ticket,
                    productCode,
                    params,
                    null);
        } catch (ProductLimitExceededException e) {
            throw new ru.nullpointer.nkbrelation.service.billing.ProductLimitExceededException(e.getMessage(), e);
        } catch (InsufficientBalanceException e) {
            throw new ru.nullpointer.nkbrelation.service.billing.InsufficientBalanceException(e.getMessage(), e);
        } catch (Exception e) { // NOPMD
            throw new ru.nullpointer.nkbrelation.service.billing.BillingException(e.getMessage(), e);
        }
    }

    @Override
    public void requestAuthenticatedProduct(String productCode, Object productParams) {
        logger.debug("productCode: {}", productCode);
        logger.debug("productParams: {}", productParams);

        SsoTicket ticket = getSsoTicket();

        Product product = getProduct(productCode);
        ensureProductAvailability(productCode, ticket);

        try {
            String params = objectMapper.writeValueAsString(productParams);
            Date requestDate = timeService.now();

            clientRequestServiceEndpoint.requestProduct(
                    ticket,
                    productCode,
                    product.getPrice(),
                    requestDate,
                    null,
                    ClientRequest.DEFAULT_PRIORITY,
                    params,
                    null);
        } catch (Exception e) { // NOPMD
            throw new ru.nullpointer.nkbrelation.service.billing.BillingException(e.getMessage(), e);
        }
    }

    public void setClientRequestServiceEndpoint(ClientRequestServiceEndpoint clientRequestServiceEndpoint) {
        this.clientRequestServiceEndpoint = clientRequestServiceEndpoint;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(clientRequestServiceEndpoint, "'clientRequestServiceEndpoint' must be set");
    }

    private Product getProduct(String productCode) {
        Product product = clientRequestServiceEndpoint.findProductByCode(productCode);

        if (product == null) {
            throw new NotFoundException("No product by code: " + productCode);
        }
        return product;
    }

    private void ensureProductAvailability(String productCode, SsoTicket ticket) {
        try {
            PurchasePossibility purchasePossibility = clientRequestServiceEndpoint.getPurchasePossibility(ticket, productCode);

            if (!purchasePossibility.isPossible()) {
                throw new ru.nullpointer.nkbrelation.service.billing.ProductNotPossibleException("Product is not possible: " + productCode);
            }
        } catch (AuthException e) {
            throw new ru.nullpointer.nkbrelation.service.billing.BillingException(e.getMessage(), e);
        }
    }

    private SsoTicket getSsoTicket() {
        Authentication auth = securityService.getAuthentication();
        if (auth == null) {
            securityService.accessDenied("Not authenticated");
        }
        TicketPrincipal p = (TicketPrincipal) auth.getPrincipal();
        return (SsoTicket) p.getTicket();
    }
}
