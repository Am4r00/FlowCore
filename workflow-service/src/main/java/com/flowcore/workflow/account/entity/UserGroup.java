package com.flowcore.workflow.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Table(name = "user_group")
@Entity
public class UserGroup {

    @Id
    private UUID id;
    @Column(name = "name_group", nullable = false, length = 150)
    private String nameGroup;
    @Column(name = "description", nullable = false, length = 350)
    private String description;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UserGroup() {
    }

    public UserGroup(UUID id, String nameGroup, String description) {
        this.id = id;
        this.nameGroup = nameGroup;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
