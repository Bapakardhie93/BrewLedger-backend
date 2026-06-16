package com.brewledger.brewledger.backend.dto.approval;

import lombok.Setter;

@Setter
public class RejectApprovalRequest {

    private String rejectReason;
    private String reason;

    public String getRejectReason() {
        if (rejectReason != null && !rejectReason.trim().isEmpty()) {
            return rejectReason;
        }
        return reason;
    }

    public String getReason() {
        if (reason != null && !reason.trim().isEmpty()) {
            return reason;
        }
        return rejectReason;
    }

    public String getEffectiveReason() {
        String eff = getRejectReason();
        return eff != null ? eff.trim() : "";
    }
}
