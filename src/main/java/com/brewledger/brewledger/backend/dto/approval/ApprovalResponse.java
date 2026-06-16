package com.brewledger.brewledger.backend.dto.approval;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApprovalResponse {
    private Long id;
    private String requestNumber;
    private String type;
    private String status;
    private String requestedByUsername;
    private String approvedByUsername;
    private String reason;
    private String rejectReason;
    private Long referenceId;
    private String payloadJson;
    private LocalDateTime createdAt;
    private String requestedByName;
    private LocalDateTime requestedAt;
    private String requestedByRole;
    private String targetRole;
}
