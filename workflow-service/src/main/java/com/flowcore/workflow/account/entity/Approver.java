package com.flowcore.workflow.account.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Table(name = "approvers")
@Entity
public class Approver  implements Serializable {

    @EmbeddedId
    private ApproverId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("departmentId")
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id")
    private UserAccount userAccount;

    public Approver() {
    }

    public Approver(Department department, UserAccount userAccount, Short approvalPriority) {
        this.id = new ApproverId(department.getId(), approvalPriority);
        this.department = department;
        this.userAccount = userAccount;
    }
}
