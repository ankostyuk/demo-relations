package ru.nullpointer.nkbrelation.api.rest.open.domain;

/**
 * @author ankostyuk
 */
public class Phone extends PublicNode {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Phone{"
                + "id=" + id
                + ", value=" + value
                + '}';
    }
}
