package com.flowcore.workflow.account.repository;

import com.flowcore.workflow.account.entity.Approver;
import com.flowcore.workflow.account.entity.ApproverId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproverRepository  extends JpaRepository<Approver, ApproverId> {
}
