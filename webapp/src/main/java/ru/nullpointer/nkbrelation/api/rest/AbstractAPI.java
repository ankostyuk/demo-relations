package ru.nullpointer.nkbrelation.api.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.nullpointer.nkbrelation.dao.DaoException;
import ru.nullpointer.nkbrelation.service.BadArgumentException;
import ru.nullpointer.nkbrelation.service.NotFoundException;
import ru.nullpointer.nkbrelation.service.ServiceException;
import ru.nullpointer.nkbrelation.service.billing.BillingException;
import ru.nullpointer.nkbrelation.service.billing.InsufficientBalanceException;
import ru.nullpointer.nkbrelation.service.billing.ProductLimitExceededException;
import ru.nullpointer.nkbrelation.service.billing.ProductNotPossibleException;

/**
 *
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
// TODO Error localization
public abstract class AbstractAPI { // NOPMD

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    //

    @ExceptionHandler(ServiceException.class)
    public void handleServiceException(ServiceException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, Error.SYSTEM);
    }

    @ExceptionHandler(NotFoundException.class)
    public void handleNotFoundException(NotFoundException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_NOT_FOUND, e, Error.SYSTEM);
    }

    @ExceptionHandler(BadArgumentException.class)
    public void handleBadArgumentException(BadArgumentException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_BAD_REQUEST, e, Error.SYSTEM);
    }

    @ExceptionHandler(BillingException.class)
    public void handleBillingException(BillingException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, Error.SYSTEM);
    }

    @ExceptionHandler(ProductNotPossibleException.class)
    public void handleProductNotPossibleException(ProductNotPossibleException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_PAYMENT_REQUIRED, e, Error.BILLING_PRODUCT_NOT_POSSIBLE);
    }

    @ExceptionHandler(ProductLimitExceededException.class)
    public void handleProductLimitExceededException(ProductLimitExceededException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_PAYMENT_REQUIRED, e, Error.BILLING_PRODUCT_LIMIT);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public void handleInsufficientBalanceException(InsufficientBalanceException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_PAYMENT_REQUIRED, e, Error.BILLING_BALANCE_INSUFFICIENT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public void handleAuthenticationException(AuthenticationException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_FORBIDDEN, e, Error.ACCESS_DENIED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_FORBIDDEN, e, Error.ACCESS_DENIED);
    }

    @ExceptionHandler(DaoException.class)
    public void handleDaoException(DaoException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, Error.SYSTEM);
    }

    @ExceptionHandler(BindException.class)
    public void handleBindException(BindException e, HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_BAD_REQUEST, e, Error.SYSTEM);
    }

    private void error(HttpServletResponse r, int code, Exception e, Error error) throws IOException {
        r.setStatus(code);
        r.getWriter().print(error.getErrorCode());
        logger.info("API exception was thrown: {}: {}", e.getClass().getCanonicalName(), e.getMessage());
        logger.debug("", e);
    }

    private enum Error {

        ACCESS_DENIED("error.access.denied"),
        BILLING_PRODUCT_NOT_POSSIBLE("error.billing.product.not.possible"),
        BILLING_PRODUCT_LIMIT("error.billing.product.limit"),
        BILLING_BALANCE_INSUFFICIENT("error.billing.balance.insufficient"),
        SYSTEM("error.system");
        //
        private String errorCode;

        Error(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
