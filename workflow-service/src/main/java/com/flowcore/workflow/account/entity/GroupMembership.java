package com.flowcore.workflow.account.entity;

import jakarta.persistence.*;

@Table(name = "group_membership")
@Entity
public class GroupMembership {

    @EmbeddedId
    private GroupMembershipId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userAccountId")
    @JoinColumn(name = "user_account_id")
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userGroupId")
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    protected GroupMembership() {
    }

    public GroupMembership(UserAccount userAccount, UserGroup userGroup) {
        this.id = new GroupMembershipId(userAccount.getId(),userGroup.getId());
        this.userAccount = userAccount;
        this.userGroup = userGroup;
    }
}
