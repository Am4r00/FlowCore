package com.flowcore.workflow.account.repository;

import com.flowcore.workflow.account.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
}
