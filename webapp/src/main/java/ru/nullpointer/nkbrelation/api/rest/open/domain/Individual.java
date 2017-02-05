package ru.nullpointer.nkbrelation.api.rest.open.domain;

/**
 * @author ankostyuk
 */
public class Individual extends PublicNode {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Individual{"
                + "id=" + id
                + ", name=" + name
                + '}';
    }
}
