package ru.nullpointer.nkbrelation.service.billing;

/**
 *
 * @author ankostyuk
 */
public interface BillingService {

    /**
     * Запрос о новой операции по продукту.
     *
     */
    void requestAuthenticatedOperation(String productCode, Object operationParams);

    /**
     * Запрос продукта.
     *
     */
    void requestAuthenticatedProduct(String productCode, Object productParams);
}
