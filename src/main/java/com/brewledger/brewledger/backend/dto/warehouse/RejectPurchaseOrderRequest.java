package com.brewledger.brewledger.backend.dto.warehouse;

import lombok.Setter;

@Setter
public class RejectPurchaseOrderRequest {

    private String reason;
    private String rejectReason;

    public String getReason() {
        if (reason != null && !reason.trim().isEmpty()) {
            return reason;
        }
        return rejectReason;
    }

    public String getRejectReason() {
        if (rejectReason != null && !rejectReason.trim().isEmpty()) {
            return rejectReason;
        }
        return reason;
    }

    public String getEffectiveReason() {
        String eff = getReason();
        return eff != null ? eff.trim() : "";
    }
}
