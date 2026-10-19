package com.hospitality.mis.dto.finance;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class FinanceBackofficeDtos {
    private FinanceBackofficeDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record OtaReconciliationResponse(Long reservationId, String guestName, String roomId,
                                            String bookingSource, BigDecimal grossRevenue,
                                            BigDecimal commission, BigDecimal netRevenue,
                                            String status, LocalDateTime bookedAt) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record OtaStatusRequest(@NotBlank String status) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record VatInvoiceResponse(Long id, Long invoiceId, String vatInvoiceNumber, BigDecimal taxRate,
                                     BigDecimal taxableAmount, BigDecimal taxAmount, BigDecimal totalAmount,
                                     String customerType, String customerName, String taxCode,
                                     String companyName, String companyAddress, String status,
                                     String xmlStatus, LocalDateTime issuedAt, String createdBy) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record VatInvoiceRequest(@NotNull Long invoiceId, @NotBlank String customerType,
                                    @NotBlank String customerName, String taxCode,
                                    String companyName, String companyAddress,
                                    BigDecimal taxableAmount) {}

}
