package com.hospitality.mis.billing;

import com.hospitality.mis.billing.domain.PaymentMethod;
import com.hospitality.mis.billing.domain.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CanonicalBillingTypesTest {
    @Test
    void paymentTypesUseCanonicalWireValues() {
        assertEquals("BANK_TRANSFER", PaymentMethod.BANK_TRANSFER.name());
        assertEquals("DA_THANH_TOAN", PaymentStatus.DA_THANH_TOAN.name());
    }
}
