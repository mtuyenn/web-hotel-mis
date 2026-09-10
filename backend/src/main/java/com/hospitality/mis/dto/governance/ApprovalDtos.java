package com.hospitality.mis.dto.governance;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.governance.ApprovalRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class ApprovalDtos {
    private ApprovalDtos() {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Request(
            @NotBlank String action,
            @NotBlank String targetId,
            @NotBlank String payload,
            @PositiveOrZero BigDecimal amount,
            @NotBlank String reason,
            @Size(max = 100) String idempotencyKey) {
    }

    /** Public approval representation; persistence fingerprints are deliberately not exposed. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(
            Long id,
            String requester,
            String action,
            String targetId,
            String payload,
            BigDecimal amount,
            String reason,
            String status,
            String approver,
            Instant decidedAt,
            Instant expiresAt,
            Instant consumedAt,
            String idempotencyKey) {

        public static Response from(ApprovalRequest approval) {
            if (approval == null) {
                return null;
            }
            return new Response(
                    approval.getId(),
                    approval.getRequester(),
                    approval.getAction(),
                    approval.getTargetId(),
                    approval.getMutationPayload(),
                    approval.getAmount(),
                    approval.getReason(),
                    approval.getStatus(),
                    approval.getApprover(),
                    approval.getDecidedAt(),
                    approval.getExpiresAt(),
                    approval.getConsumedAt(),
                    approval.getCorrelationKey());
        }
    }
}
