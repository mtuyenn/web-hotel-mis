package com.hospitality.mis.dto.billing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.billing.PaymentMethod;
import com.hospitality.mis.entity.billing.PaymentTransaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PaymentTransactionDtos {
    private PaymentTransactionDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotNull @Positive BigDecimal amount, @NotNull PaymentMethod method,
                                @NotNull PaymentTransaction.TransactionType type, String reference,
                                @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 35) String idempotencyKey) {
        public String approvalPayload(Long invoiceId) {
            return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode()
                    .put("invoice_id", invoiceId).put("idempotency_key", idempotencyKey.trim())
                    .put("method", method.name()).put("type", type.name())
                    .put("reference", reference == null ? "" : reference).toString();
        }
    }
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long invoiceId, BigDecimal amount, PaymentMethod method,
                           PaymentTransaction.TransactionType type, PaymentTransaction.TransactionStatus status,
                           String reference, LocalDateTime occurredAt, String actorId) {}
}
