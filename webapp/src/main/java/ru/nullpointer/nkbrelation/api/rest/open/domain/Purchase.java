package ru.nullpointer.nkbrelation.api.rest.open.domain;

/**
 * @author ankostyuk
 */
public class Purchase extends PublicNode {

    private String name;
    private String form;
    private String date;
    private String currency;
    private Number totalPrice;
    private String etp;
    private String law;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Number getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Number totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getEtp() {
        return etp;
    }

    public void setEtp(String etp) {
        this.etp = etp;
    }

    public String getLaw() {
        return law;
    }

    public void setLaw(String law) {
        this.law = law;
    }

    @Override
    public String toString() {
        return "Purchase{"
                + "id=" + id
                + ", name=" + name
                + ", date=" + date
                + ", totalPrice=" + totalPrice
                + '}';
    }
}
