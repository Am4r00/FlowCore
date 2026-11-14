package com.flowcore.workflow.account.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class GroupMembershipId implements Serializable {

    @Column(name = "user_account_id", nullable = false)
    private UUID userAccountId;

    @Column(name = "user_group_id", nullable = false)
    private UUID userGroupId;

    public GroupMembershipId() {
    }

    public GroupMembershipId(UUID userAccountId, UUID userGroupId) {
        this.userAccountId = userAccountId;
        this.userGroupId = userGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GroupMembershipId that = (GroupMembershipId) o;
        return Objects.equals(userAccountId, that.userAccountId) && Objects.equals(userGroupId, that.userGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userAccountId, userGroupId);
    }

}
