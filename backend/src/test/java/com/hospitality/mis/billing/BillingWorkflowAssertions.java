package com.hospitality.mis.billing;

import com.hospitality.mis.entity.billing.Invoice;
import com.hospitality.mis.entity.guest.Guest;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.identity.EmployeeRole;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.governance.ApprovalRequest;
import com.hospitality.mis.service.governance.ApprovalService;
import com.hospitality.mis.dto.billing.PaymentTransactionDtos;
import com.hospitality.mis.entity.billing.PaymentMethod;
import com.hospitality.mis.entity.billing.PaymentTransaction;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/** HTTP -> real services -> real repositories. Runs against both H2 and MySQL. */
@Transactional
@WithMockUser(username = "clerk", roles = "FRONT_DESK")
public abstract class BillingWorkflowAssertions {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    private Invoice invoice;

    @BeforeEach void seedInvoice() {
        Employee employee = new Employee(); employee.setEmployeeId("clerk"); employee.setFullName("Clerk");
        employee.setPassword("test-hash"); employee.setPhone("0900000091"); employee.setRole(EmployeeRole.FRONT_DESK); em.persist(employee);
        Guest guest = new Guest(); guest.setFullName("Billing Guest"); guest.setPhone("0900000092");
        guest.setIdentityNumber("012345678991"); em.persist(guest);
        Reservation r = new Reservation(); r.setEmployee(employee); r.setGuest(guest); em.persist(r);
        invoice = new Invoice(); invoice.setReservation(r); invoice.setRoomTotal(new BigDecimal("1250500"));
        em.persist(invoice); em.flush();
    }

    @Test void collectRoundedTotalIssueReceiptRefundAndReplayWithoutDuplicate() throws Exception {
        payment("1251000", "PAYMENT", "collect");
        mvc.perform(get("/api/invoices/reservation/{id}", invoice.getReservation().getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.payable").value(0));
        mvc.perform(post("/api/invoices/{id}/receipts", invoice.getId()).contentType(APPLICATION_JSON)
                .content("{\"receipt_number\":\"TEST-RECEIPT\",\"amount\":1251000,\"method\":\"CASH\"}"))
                .andExpect(status().isOk());
        approveRefund("251000", "refund");
        payment("251000", "REFUND", "refund");
        payment("251000", "REFUND", "refund");
        em.flush(); em.clear();
        mvc.perform(get("/api/invoices/{id}/payments", invoice.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/invoices/reservation/{id}", invoice.getReservation().getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.payable").value(251000));
    }

    @Test @WithMockUser(username = "other", roles = "FRONT_DESK")
    void nextShiftCanReadReservationFinancialDocuments() throws Exception {
        mvc.perform(get("/api/invoices/reservation/{id}", invoice.getReservation().getId())).andExpect(status().isOk());
        mvc.perform(get("/api/invoices/{id}/payments", invoice.getId())).andExpect(status().isOk());
        mvc.perform(get("/api/invoices/{id}/receipts", invoice.getId())).andExpect(status().isOk());
    }

    @Test void missingIdempotencyKeyIsRejectedAtHttpBoundary() throws Exception {
        mvc.perform(post("/api/invoices/{id}/payments", invoice.getId()).contentType(APPLICATION_JSON)
                .content("{\"amount\":1000,\"method\":\"CASH\",\"type\":\"PAYMENT\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test void overpaymentIsRejectedWithoutRecordingMoney() throws Exception {
        mvc.perform(post("/api/invoices/{id}/payments", invoice.getId()).contentType(APPLICATION_JSON)
                .content(body("1252000", "PAYMENT", "overpay")))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("PAYMENT_EXCEEDS_BALANCE"));
        mvc.perform(get("/api/invoices/{id}/payments", invoice.getId())).andExpect(jsonPath("$.length()").value(0));
    }

    private void payment(String amount, String type, String key) throws Exception {
        mvc.perform(post("/api/invoices/{id}/payments", invoice.getId()).contentType(APPLICATION_JSON)
                .content(body(amount, type, key))).andExpect(status().isOk());
    }

    private void approveRefund(String amount, String key) throws Exception {
        var request = new PaymentTransactionDtos.CreateRequest(new BigDecimal(amount), PaymentMethod.CASH,
                PaymentTransaction.TransactionType.REFUND, "guest request", key);
        String payload = request.approvalPayload(invoice.getId());
        ApprovalRequest approval = new ApprovalRequest("clerk", "PAYMENT_REFUND", String.valueOf(invoice.getId()),
                payload, ApprovalService.fingerprintFor(payload), new BigDecimal(amount), "approved refund",
                Instant.now().plusSeconds(3600), "approval-" + key);
        em.persist(approval); em.flush();
        mvc.perform(post("/api/governance/approvals/{id}/approve", approval.getId())
                .with(user("director").roles("DIRECTOR"))).andExpect(status().isOk());
    }
    private String body(String amount, String type, String key) {
        return "{\"amount\":" + amount + ",\"method\":\"CASH\",\"type\":\"" + type
                + "\",\"reference\":\"guest request\",\"idempotency_key\":\"" + key + "\"}";
    }
}
