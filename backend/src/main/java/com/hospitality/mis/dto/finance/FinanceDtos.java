package com.hospitality.mis.dto.finance;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.finance.Expense;
import com.hospitality.mis.entity.finance.PartnerDebt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class FinanceDtos {
    private FinanceDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CashHandoverRequest(@NotBlank String shiftCode, @NotBlank String fromActor,
                                      @NotBlank String toActor, @NotNull BigDecimal actualAmount, String note) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CashHandoverResponse(Long id, String shiftCode, String fromActor, String toActor,
                                       BigDecimal expectedAmount, BigDecimal actualAmount, BigDecimal variance,
                                       LocalDateTime handedOverAt, String note) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ExpenseRequest(@NotBlank String category, @NotBlank String description,
                                 @NotNull @Positive BigDecimal amount) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ExpenseResponse(Long id, String category, String description, BigDecimal amount,
                                  String paidBy, LocalDateTime paidAt, Expense.ExpenseStatus status) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record PartnerDebtRequest(@NotBlank String partnerName, @NotBlank String referenceCode,
                                     @NotNull @Positive BigDecimal amount) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record PartnerDebtResponse(Long id, String partnerName, String referenceCode, BigDecimal amount,
                                      BigDecimal settledAmount, PartnerDebt.DebtStatus status,
                                      LocalDateTime recordedAt) {}
}
