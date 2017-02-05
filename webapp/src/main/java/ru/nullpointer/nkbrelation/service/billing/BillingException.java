package ru.nullpointer.nkbrelation.service.billing;

import ru.nullpointer.nkbrelation.service.ServiceException;

/**
 *
 * @author ankostyuk
 */
public class BillingException extends ServiceException {

    private static final long serialVersionUID = 20120912;

    public BillingException() {
        super();
    }

    public BillingException(String m) {
        super(m);
    }

    public BillingException(String m, Exception e) {
        super(m, e);
    }
}
