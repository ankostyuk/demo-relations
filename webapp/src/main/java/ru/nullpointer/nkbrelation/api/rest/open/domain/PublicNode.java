package ru.nullpointer.nkbrelation.api.rest.open.domain;

import ru.nullpointer.nkbrelation.domain.NodeInfo;

import java.util.List;
import java.util.Map;

/**
 * @author ankostyuk
 */
public abstract class PublicNode {

    protected String id;
    protected NodeInfo info;
    protected List<Map<String, Object>> relations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeInfo getInfo() {
        return info;
    }

    public void setInfo(NodeInfo info) {
        this.info = info;
    }

    public List<Map<String, Object>> getRelations() {
        return relations;
    }

    public void setRelations(List<Map<String, Object>> relations) {
        this.relations = relations;
    }
}
