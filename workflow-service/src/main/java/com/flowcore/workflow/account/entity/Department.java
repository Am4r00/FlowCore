package com.flowcore.workflow.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Table(name = "department")
@Entity
public class Department {

    @Id
    private UUID id;

    @Column(name = "department_name", nullable = false,length = 150)
    private String departmentName;

    public Department() {
    }

    public Department(UUID id, String departmentName) {
        this.id = id;
        this.departmentName = departmentName;
    }


    public UUID getId() {
        return id;
    }

    public String getDepartmentName() {
        return departmentName;
    }
}
