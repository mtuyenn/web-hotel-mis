package com.hospitality.mis.billing.api;

import com.hospitality.mis.billing.domain.PaymentMethod;
import com.hospitality.mis.billing.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class InvoiceDtos {
    private InvoiceDtos() {}
    public record Response(Long id, Long reservationId, LocalDateTime issuedAt, BigDecimal roomTotal,
                           BigDecimal serviceTotal, BigDecimal lateSurcharge, BigDecimal compensation,
                           BigDecimal extensionTotal, BigDecimal discount, BigDecimal deposit,
                           BigDecimal payable, PaymentMethod paymentMethod, PaymentStatus status) {}
}
