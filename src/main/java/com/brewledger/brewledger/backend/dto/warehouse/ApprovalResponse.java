package com.brewledger.brewledger.backend.dto.warehouse;

import java.time.LocalDateTime;

public class ApprovalResponse {
    private final Long id;
    private final String requestNumber;
    private final String type;
    private final String status;
    private final String requestedByUsername;
    private final String approvedByUsername;
    private final String reason;
    private final String rejectReason;
    private final Long referenceId;
    private final String payloadJson;
    private final LocalDateTime createdAt;
    private final String requestedByName;
    private final LocalDateTime requestedAt;
    private final String requestedByRole;
    private final String targetRole;

    public ApprovalResponse(Long id, String requestNumber, String type, String status, String requestedByUsername,
                            String approvedByUsername, String reason, String rejectReason, Long referenceId,
                            String payloadJson, LocalDateTime createdAt, String requestedByName,
                            LocalDateTime requestedAt, String requestedByRole, String targetRole) {
        this.id = id;
        this.requestNumber = requestNumber;
        this.type = type;
        this.status = status;
        this.requestedByUsername = requestedByUsername;
        this.approvedByUsername = approvedByUsername;
        this.reason = reason;
        this.rejectReason = rejectReason;
        this.referenceId = referenceId;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
        this.requestedByName = requestedByName;
        this.requestedAt = requestedAt;
        this.requestedByRole = requestedByRole;
        this.targetRole = targetRole;
    }

    public Long getId() {
        return id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getRequestedByUsername() {
        return requestedByUsername;
    }

    public String getApprovedByUsername() {
        return approvedByUsername;
    }

    public String getReason() {
        return reason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public String getRequestedByRole() {
        return requestedByRole;
    }

    public String getTargetRole() {
        return targetRole;
    }
}
