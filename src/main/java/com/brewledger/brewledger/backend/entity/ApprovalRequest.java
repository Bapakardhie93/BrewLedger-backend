package com.brewledger.brewledger.backend.entity;

import com.brewledger.brewledger.backend.enums.ApprovalStatus;
import com.brewledger.brewledger.backend.enums.ApprovalType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "approval_requests")
public class ApprovalRequest extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String requestNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    private String reason;

    private String rejectReason;

    private Long referenceId;

    @Column(columnDefinition = "TEXT")
    private String payloadJson;

    private String requestedByRole;

    private String targetRole;
}
