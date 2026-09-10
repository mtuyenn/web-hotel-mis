package com.hospitality.mis.dto.billing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


import com.hospitality.mis.entity.billing.PaymentMethod;

import com.hospitality.mis.entity.billing.PaymentStatus;


import java.math.BigDecimal;

import java.time.LocalDateTime;



public final class InvoiceDtos {

    private InvoiceDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long reservationId, LocalDateTime issuedAt, BigDecimal roomTotal,

                           BigDecimal serviceTotal, BigDecimal lateSurcharge, BigDecimal compensation,

                           BigDecimal extensionTotal, BigDecimal adjustmentTotal, BigDecimal discount, BigDecimal deposit,

                           BigDecimal payable, PaymentMethod paymentMethod, PaymentStatus status) {}
}
