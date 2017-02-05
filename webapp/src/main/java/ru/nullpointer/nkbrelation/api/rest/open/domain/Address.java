package ru.nullpointer.nkbrelation.api.rest.open.domain;

/**
 * @author ankostyuk
 */
public class Address extends PublicNode {

    private String index;
    private String value;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Address{"
                + "id=" + id
                + ", index=" + index
                + ", value=" + value
                + '}';
    }
}
