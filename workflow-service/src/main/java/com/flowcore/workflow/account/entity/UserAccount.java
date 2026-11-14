package com.flowcore.workflow.account.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name ="user_account")
public class UserAccount {

    @Id
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(nullable = false, length = 100)
    private String login;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JoinColumn(name = "department_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    @Column(name = "job_title",length = 100)
    private String jobTitle;

    protected UserAccount() {
    }

    public UserAccount(String displayName, String login, String email, String passwordHash, Department department, String jobTitle) {
        this.id = UUID.randomUUID();
        this.displayName = displayName;
        this.login = login;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = true;
        this.createdAt = Instant.now();
        this.department = department;
        this.jobTitle = jobTitle;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Department getDepartment() {
        return department;
    }

    public String getJobTitle() {
        return jobTitle;
    }
}
