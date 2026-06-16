package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
}
