package com.hospitality.mis.billing;

import com.hospitality.mis.entity.billing.PaymentMethod;
import com.hospitality.mis.entity.billing.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Bảo vệ các enum billing dùng đúng giá trị wire/schema hiện hành. */
class CanonicalBillingTypesTest {
    /** Given enum canonical, When serialize bằng name, Then không quay lại mã legacy. */
    @Test
    void paymentTypesUseCanonicalWireValues() {
        assertEquals("BANK_TRANSFER", PaymentMethod.BANK_TRANSFER.name());
        assertEquals("DA_THANH_TOAN", PaymentStatus.DA_THANH_TOAN.name());
    }
}
