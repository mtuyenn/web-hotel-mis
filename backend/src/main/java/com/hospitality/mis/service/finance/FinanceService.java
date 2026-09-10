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

@Service
public class FinanceService {
    private final CashShiftHandoverRepository handovers; private final ExpenseRepository expenses; private final PartnerDebtRepository debts;
    private final AuditService audit;
    private final PaymentTransactionRepository transactions;
    @org.springframework.beans.factory.annotation.Autowired
    public FinanceService(CashShiftHandoverRepository handovers, ExpenseRepository expenses, PartnerDebtRepository debts,
                          AuditService audit, PaymentTransactionRepository transactions) {
        this.handovers = handovers; this.expenses = expenses; this.debts = debts; this.audit = audit; this.transactions = transactions;
    }
    public FinanceService(CashShiftHandoverRepository handovers, ExpenseRepository expenses, PartnerDebtRepository debts, AuditService audit) {
        this(handovers, expenses, debts, audit, null);
    }

    @Transactional
    public FinanceDtos.CashHandoverResponse handover(FinanceDtos.CashHandoverRequest request, String actor) {
        actor = SecurityActor.requireBoundActor(actor);
        if (!actor.equals(request.fromActor())) throw new DomainException("ACTOR_MISMATCH", "from_actor phải là actor đã xác thực");
        if (request.actualAmount().signum() < 0)
            throw new DomainException("INVALID_HANDOVER_AMOUNT", "Số tiền bàn giao không thể âm");
        if (request.fromActor().equals(request.toActor()))
            throw new DomainException("INVALID_HANDOVER_ACTORS", "Người giao và người nhận ca phải khác nhau");
        LocalDateTime handedOverAt = LocalDateTime.now();
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

    @Transactional
    public FinanceDtos.ExpenseResponse recordExpense(FinanceDtos.ExpenseRequest request, String actor) {
        if (actor == null || actor.isBlank()) throw new DomainException("ACTOR_REQUIRED", "Thiếu actor thực hiện");
        Expense e = new Expense(); e.setCategory(request.category()); e.setDescription(request.description()); e.setAmount(request.amount()); e.setPaidBy(actor); e.setPaidAt(LocalDateTime.now());
        e = expenses.save(e);
        audit.record(actor, "EXPENSE_RECORDED", "EXPENSE", String.valueOf(e.getId()), null, e.getAmount().toPlainString(), null);
        return toResponse(e);
    }

    @Transactional
    public FinanceDtos.PartnerDebtResponse recordDebt(FinanceDtos.PartnerDebtRequest request) {
        if (debts.findByReferenceCode(request.referenceCode()).isPresent()) throw new DomainException("PARTNER_DEBT_EXISTS", "Mã công nợ đã tồn tại");
        PartnerDebt d = new PartnerDebt(); d.setPartnerName(request.partnerName()); d.setReferenceCode(request.referenceCode()); d.setAmount(request.amount()); d.setSettledAmount(BigDecimal.ZERO); d.setRecordedAt(LocalDateTime.now());
        d = debts.save(d);
        audit.record(SecurityActor.currentActor(), "PARTNER_DEBT_RECORDED", "PARTNER_DEBT", String.valueOf(d.getId()), null, d.getAmount().toPlainString(), null);
        return toResponse(d);
    }

    @Transactional(readOnly = true)
    public java.util.List<FinanceDtos.CashHandoverResponse> listHandovers() { return handovers.findAllByOrderByHandedOverAtDesc().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true)
    public java.util.List<FinanceDtos.ExpenseResponse> listExpenses() { return expenses.findAllByOrderByPaidAtDesc().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true)
    public java.util.List<FinanceDtos.PartnerDebtResponse> listDebts() { return debts.findAllByOrderByRecordedAtDesc().stream().map(this::toResponse).toList(); }

    private FinanceDtos.CashHandoverResponse toResponse(CashShiftHandover h) { return new FinanceDtos.CashHandoverResponse(h.getId(), h.getShiftCode(), h.getFromActor(), h.getToActor(), h.getExpectedAmount(), h.getActualAmount(), h.getVariance(), h.getHandedOverAt(), h.getNote()); }
    private FinanceDtos.ExpenseResponse toResponse(Expense e) { return new FinanceDtos.ExpenseResponse(e.getId(), e.getCategory(), e.getDescription(), e.getAmount(), e.getPaidBy(), e.getPaidAt(), e.getStatus()); }
    private FinanceDtos.PartnerDebtResponse toResponse(PartnerDebt d) { return new FinanceDtos.PartnerDebtResponse(d.getId(), d.getPartnerName(), d.getReferenceCode(), d.getAmount(), d.getSettledAmount(), d.getStatus(), d.getRecordedAt()); }
}
