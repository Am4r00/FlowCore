package com.flowcore.workflow.account.repository;

import com.flowcore.workflow.account.entity.GroupMembership;
import com.flowcore.workflow.account.entity.GroupMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, GroupMembershipId> {
}
