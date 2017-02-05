package ru.nullpointer.nkbrelation.service.billing;

/**
 *
 * @author ankostyuk
 */
public enum Product {

    /**
     * Поиск объектов
     */
    RELATIONS_SEARCH("relations_search"),
    /**
     * Поиск связанных объектов
     */
    RELATIONS_FIND_RELATED("relations_find_related"),
    /**
     * Поиск связей между объектами
     */
    RELATIONS_FIND_RELATIONS("relations_find_relations"),
    /**
     * Выписка ЕГРЮЛ по компании
     */
    EGRUL_COMPANY("egrulCompanyReport"),
    /**
     * Выписка по учредителю физическому лицу
     */
    EGRUL_INDIVIDUAL_FOUNDER("egrulFounderPersonReport"),
    /**
     * Выписка по руководителю физическому лицу
     */
    EGRUL_INDIVIDUAL_EXECUTIVE("egrulChiefReport");
    //
    private final String productCode;

    Product(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }
}
