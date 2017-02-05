package ru.nullpointer.nkbrelation.domain;

import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author ankostyuk
 */
public class UserInfo {

    private String userId;
    private Set<String> permissions;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
