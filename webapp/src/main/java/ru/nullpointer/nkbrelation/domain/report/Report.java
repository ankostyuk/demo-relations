package ru.nullpointer.nkbrelation.domain.report;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public class Report {

    private String id;
    private String userId;
    private String shareKey;
    private Boolean shared;
    //
    private String name;
    private String comment;
    private Date created;
    private Date edited;
    //
    private Map<String, Object> style;
    //
    private List<Map<String, Object>> nodes;
    private List<Map<String, Object>> relations;
    //
    private List<Link> links;
    //
    private List<Map<String, Object>> userObjects;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShareKey() {
        return shareKey;
    }

    public void setShareKey(String shareKey) {
        this.shareKey = shareKey;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getEdited() {
        return edited;
    }

    public void setEdited(Date edited) {
        this.edited = edited;
    }

    public Map<String, Object> getStyle() {
        return style;
    }

    public void setStyle(Map<String, Object> style) {
        this.style = style;
    }

    public List<Map<String, Object>> getNodes() {
        return nodes;
    }

    public void setNodes(List<Map<String, Object>> nodes) {
        this.nodes = nodes;
    }

    public List<Map<String, Object>> getRelations() {
        return relations;
    }

    public void setRelations(List<Map<String, Object>> relations) {
        this.relations = relations;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<Map<String, Object>> getUserObjects() {
        return userObjects;
    }

    public void setUserObjects(List<Map<String, Object>> userObjects) {
        this.userObjects = userObjects;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
