package com.flowcore.workflow.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ApproverId implements Serializable {

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "approval_priority",nullable = false)
    private Short approvalPriority;

    public ApproverId() {
    }

    public ApproverId(UUID departmentId, Short approvalPriority) {
        this.departmentId = departmentId;
        this.approvalPriority = approvalPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApproverId that = (ApproverId) o;
        return Objects.equals(departmentId, that.departmentId) && Objects.equals(approvalPriority, that.approvalPriority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId, approvalPriority);
    }
}
