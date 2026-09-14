package com.hospitality.mis.service.finance;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.finance.CashShiftHandoverRepository;
import com.hospitality.mis.dao.finance.ExpenseRepository;
import com.hospitality.mis.dao.finance.PartnerDebtRepository;
import com.hospitality.mis.dao.billing.PaymentTransactionRepository;
import com.hospitality.mis.dto.finance.FinanceDtos;
import com.hospitality.mis.entity.finance.CashShiftHandover;
import com.hospitality.mis.entity.finance.Expense;
import com.hospitality.mis.entity.finance.PartnerDebt;
import org.springframework.stereotype.Service;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.middleware.security.SecurityActor;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import com.hospitality.mis.entity.billing.PaymentTransaction;

/** Ghi nhận bàn giao ca, chi phí và công nợ đối tác, kèm audit tài chính. */
@Service
public class FinanceService {
    /** Các kho sổ ca, chi phí và công nợ; giao dịch ghi được bao bọc bởi @Transactional. */
    private final CashShiftHandoverRepository handovers; private final ExpenseRepository expenses; private final PartnerDebtRepository debts;
    /** Audit người thực hiện và giá trị tài chính thay đổi. */
    private final AuditService audit;
    /** Tính tiền mặt ròng của actor giữa hai thời điểm bàn giao. */
    private final PaymentTransactionRepository transactions;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    @org.springframework.beans.factory.annotation.Autowired
    public FinanceService(CashShiftHandoverRepository handovers, ExpenseRepository expenses, PartnerDebtRepository debts,
                          AuditService audit, PaymentTransactionRepository transactions) {
        this.handovers = handovers; this.expenses = expenses; this.debts = debts; this.audit = audit; this.transactions = transactions;
    }
    public FinanceService(CashShiftHandoverRepository handovers, ExpenseRepository expenses, PartnerDebtRepository debts, AuditService audit) {
        this(handovers, expenses, debts, audit, null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }

    /** Tính tiền mặt kỳ ca trước, kiểm tra actor và lưu chênh lệch bàn giao. */
    @Transactional
    public FinanceDtos.CashHandoverResponse handover(FinanceDtos.CashHandoverRequest request, String actor) {
        actor = SecurityActor.requireBoundActor(actor);
        if (!actor.equals(request.fromActor())) throw new DomainException("ACTOR_MISMATCH", "from_actor phải là actor đã xác thực");
        if (request.actualAmount().signum() < 0)
            throw new DomainException("INVALID_HANDOVER_AMOUNT", "Số tiền bàn giao không thể âm");
        if (request.fromActor().equals(request.toActor()))
            throw new DomainException("INVALID_HANDOVER_ACTORS", "Người giao và người nhận ca phải khác nhau");
        LocalDateTime handedOverAt = LocalDateTime.now(clock);
        LocalDateTime fromAt = handovers.findFirstByFromActorOrderByHandedOverAtDesc(actor)
                .map(CashShiftHandover::getHandedOverAt).orElse(LocalDateTime.of(1970, 1, 1, 0, 0));
        BigDecimal expected = transactions.netCashByActorBetween(actor, fromAt, handedOverAt);
        if (expected == null) expected = BigDecimal.ZERO;
        CashShiftHandover h = new CashShiftHandover(); h.setShiftCode(request.shiftCode()); h.setFromActor(request.fromActor()); h.setToActor(request.toActor());
        h.setExpectedAmount(expected); h.setActualAmount(request.actualAmount()); h.setVariance(request.actualAmount().subtract(expected));
        h.setHandedOverAt(handedOverAt); h.setNote(request.note()); h = handovers.save(h);
        audit.record(actor, "CASH_HANDOVER_RECORDED", "CASH_HANDOVER", String.valueOf(h.getId()), null, h.getActualAmount().toPlainString(), request.note());
        return toResponse(h);
    }

    /** Ghi một khoản chi với actor đã thực hiện và thời điểm phát sinh. */
    @Transactional
    public FinanceDtos.ExpenseResponse recordExpense(FinanceDtos.ExpenseRequest request, String actor) {
        if (actor == null || actor.isBlank()) throw new DomainException("ACTOR_REQUIRED", "Thiếu actor thực hiện");
        Expense e = new Expense(); e.setCategory(request.category()); e.setDescription(request.description()); e.setAmount(request.amount()); e.setPaidBy(actor); e.setPaidAt(LocalDateTime.now(clock));
        e = expenses.save(e);
        audit.record(actor, "EXPENSE_RECORDED", "EXPENSE", String.valueOf(e.getId()), null, e.getAmount().toPlainString(), null);
        return toResponse(e);
    }

    /** Ghi công nợ đối tác với mã tham chiếu duy nhất và số đã thanh toán bằng không. */
    @Transactional
    public FinanceDtos.PartnerDebtResponse recordDebt(FinanceDtos.PartnerDebtRequest request) {
        if (debts.findByReferenceCode(request.referenceCode()).isPresent()) throw new DomainException("PARTNER_DEBT_EXISTS", "Mã công nợ đã tồn tại");
        PartnerDebt d = new PartnerDebt(); d.setPartnerName(request.partnerName()); d.setReferenceCode(request.referenceCode()); d.setAmount(request.amount()); d.setSettledAmount(BigDecimal.ZERO); d.setRecordedAt(LocalDateTime.now(clock));
        d = debts.save(d);
        audit.record(SecurityActor.currentActor(), "PARTNER_DEBT_RECORDED", "PARTNER_DEBT", String.valueOf(d.getId()), null, d.getAmount().toPlainString(), null);
        return toResponse(d);
    }

    @Transactional(readOnly = true)
    /** Liệt kê bàn giao ca mới nhất trước. */
    public java.util.List<FinanceDtos.CashHandoverResponse> listHandovers() { return handovers.findAllByOrderByHandedOverAtDesc().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true)
    /** Liệt kê chi phí theo thời điểm thanh toán giảm dần. */
    public java.util.List<FinanceDtos.ExpenseResponse> listExpenses() { return expenses.findAllByOrderByPaidAtDesc().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true)
    /** Liệt kê công nợ đối tác theo thời điểm ghi nhận giảm dần. */
    public java.util.List<FinanceDtos.PartnerDebtResponse> listDebts() { return debts.findAllByOrderByRecordedAtDesc().stream().map(this::toResponse).toList(); }

    @Transactional
    public FinanceDtos.PartnerDebtResponse settleDebt(Long id, FinanceDtos.DebtSettlementRequest request, String actor) {
        var debt = debts.findForUpdate(id).orElseThrow(() -> new DomainException("PARTNER_DEBT_NOT_FOUND", "Không tìm thấy công nợ đối tác"));
        if (request.amount().signum() <= 0 || debt.getSettledAmount().add(request.amount()).compareTo(debt.getAmount()) > 0)
            throw new DomainException("INVALID_DEBT_SETTLEMENT", "Số tiền tất toán vượt số dư công nợ");
        debt.setSettledAmount(debt.getSettledAmount().add(request.amount()));
        debt.setStatus(debt.getSettledAmount().compareTo(debt.getAmount()) == 0
                ? PartnerDebt.DebtStatus.SETTLED : PartnerDebt.DebtStatus.PARTIALLY_SETTLED);
        audit.record(actor, "PARTNER_DEBT_SETTLED", "PARTNER_DEBT", String.valueOf(id), null, request.amount().toPlainString(), request.note());
        return toResponse(debt);
    }

    @Transactional(readOnly = true)
    public FinanceDtos.ReconciliationResponse reconcile(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? LocalDate.now(clock).minusDays(1) : from;
        LocalDate end = to == null ? LocalDate.now(clock) : to;
        Map<String, BigDecimal> totals = new LinkedHashMap<>(); BigDecimal payments = BigDecimal.ZERO; BigDecimal refunds = BigDecimal.ZERO;
        for (PaymentTransaction tx : transactions.findAll()) {
            if (tx.getOccurredAt() == null || tx.getOccurredAt().toLocalDate().isBefore(start) || tx.getOccurredAt().toLocalDate().isAfter(end)
                    || tx.getStatus() != PaymentTransaction.TransactionStatus.COMPLETED) continue;
            String method = tx.getMethod().name(); BigDecimal signed = tx.getType() == PaymentTransaction.TransactionType.REFUND ? tx.getAmount().negate() : tx.getAmount();
            totals.merge(method, signed, BigDecimal::add);
            if (tx.getType() == PaymentTransaction.TransactionType.REFUND) refunds = refunds.add(tx.getAmount()); else payments = payments.add(tx.getAmount());
        }
        return new FinanceDtos.ReconciliationResponse(start, end, totals, payments, refunds, payments.subtract(refunds));
    }

    /** Chuyển bản ghi bàn giao ca thành DTO. */
    private FinanceDtos.CashHandoverResponse toResponse(CashShiftHandover h) { return new FinanceDtos.CashHandoverResponse(h.getId(), h.getShiftCode(), h.getFromActor(), h.getToActor(), h.getExpectedAmount(), h.getActualAmount(), h.getVariance(), h.getHandedOverAt(), h.getNote()); }
    /** Chuyển bản ghi chi phí thành DTO. */
    private FinanceDtos.ExpenseResponse toResponse(Expense e) { return new FinanceDtos.ExpenseResponse(e.getId(), e.getCategory(), e.getDescription(), e.getAmount(), e.getPaidBy(), e.getPaidAt(), e.getStatus()); }
    /** Chuyển bản ghi công nợ thành DTO. */
    private FinanceDtos.PartnerDebtResponse toResponse(PartnerDebt d) { return new FinanceDtos.PartnerDebtResponse(d.getId(), d.getPartnerName(), d.getReferenceCode(), d.getAmount(), d.getSettledAmount(), d.getStatus(), d.getRecordedAt()); }
}
