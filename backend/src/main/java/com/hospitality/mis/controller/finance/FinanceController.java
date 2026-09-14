package com.hospitality.mis.controller.finance;

import com.hospitality.mis.dto.finance.FinanceDtos;
import com.hospitality.mis.service.finance.FinanceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Điều phối các nghiệp vụ tài chính: bàn giao quỹ, chi phí và công nợ đối tác.
 */
@RestController
@RequestMapping("/api/finance")
public class FinanceController {
    /** Dịch vụ ghi nhận và tra cứu các sổ liệu tài chính; controller truyền tên người xác thực khi nghiệp vụ yêu cầu. */
    private final FinanceService service;
    public FinanceController(FinanceService service) { this.service = service; }
    /**
     * Ghi nhận bàn giao tiền mặt qua POST /api/finance/cash-handovers.
     * Body được {@code @Valid} kiểm tra, principal trong Authentication là người thực hiện, response là bản ghi bàn giao.
     * Chỉ FINANCE_WRITE được phép; không có idempotency key, còn dữ liệu sai hoặc bản ghi trùng do dịch vụ xử lý lỗi.
     */
    @PostMapping("/cash-handovers") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')") public FinanceDtos.CashHandoverResponse handover(@Valid @RequestBody FinanceDtos.CashHandoverRequest request, org.springframework.security.core.Authentication auth) { return service.handover(request, auth.getName()); }
    /**
     * Ghi nhận chi phí qua POST /api/finance/expenses; body chi phí được {@code @Valid} kiểm tra và actor lấy từ Authentication.
     * Trả bản ghi chi phí, chỉ FINANCE_WRITE được gọi; không có idempotency key nên trùng hoặc lỗi nghiệp vụ do dịch vụ báo.
     */
    @PostMapping("/expenses") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')") public FinanceDtos.ExpenseResponse expense(@Valid @RequestBody FinanceDtos.ExpenseRequest request, org.springframework.security.core.Authentication auth) { return service.recordExpense(request, auth.getName()); }
    /**
     * Ghi nhận công nợ đối tác qua POST /api/finance/partner-debts; body được {@code @Valid} kiểm tra.
     * Trả công nợ đã tạo, chỉ FINANCE_WRITE được phép; không có idempotency key và lỗi dữ liệu/trạng thái do dịch vụ xử lý.
     */
    @PostMapping("/partner-debts") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')") public FinanceDtos.PartnerDebtResponse debt(@Valid @RequestBody FinanceDtos.PartnerDebtRequest request) { return service.recordDebt(request); }
    /**
     * Liệt kê bàn giao tiền mặt qua GET /api/finance/cash-handovers; không có tham số, trả danh sách để đối soát.
     * Chỉ FINANCE_READ được phép; lỗi truy vấn do dịch vụ xử lý và thao tác đọc không cần idempotency.
     */
    @GetMapping("/cash-handovers") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')") public Object handovers(@RequestParam(required = false) String shiftCode, @RequestParam(required = false) String actor, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return shiftCode == null && actor == null && from == null && to == null && page == null && size == null ? service.listHandovers() : service.pageHandovers(shiftCode, actor, from, to, page == null ? 0 : page, size == null ? 20 : size); }
    /**
     * Liệt kê chi phí qua GET /api/finance/expenses; không có tham số và trả danh sách chi phí.
     * Chỉ FINANCE_READ được phép; lỗi truy vấn do dịch vụ xử lý, không có idempotency concern.
     */
    @GetMapping("/expenses") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')") public Object expenses(@RequestParam(required = false) String category, @RequestParam(required = false) com.hospitality.mis.entity.finance.Expense.ExpenseStatus status, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return category == null && status == null && from == null && to == null && page == null && size == null ? service.listExpenses() : service.pageExpenses(category, status, from, to, page == null ? 0 : page, size == null ? 20 : size); }
    /**
     * Liệt kê công nợ đối tác qua GET /api/finance/partner-debts; không có tham số và trả danh sách công nợ.
     * Chỉ FINANCE_READ được phép; lỗi truy vấn do dịch vụ xử lý, không có idempotency concern.
     */
    @GetMapping("/partner-debts") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')") public Object debts(@RequestParam(required = false) String partner, @RequestParam(required = false) com.hospitality.mis.entity.finance.PartnerDebt.DebtStatus status, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return partner == null && status == null && from == null && to == null && page == null && size == null ? service.listDebts() : service.pageDebts(partner, status, from, to, page == null ? 0 : page, size == null ? 20 : size); }

    @PostMapping("/partner-debts/{id}/settle")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')")
    public FinanceDtos.PartnerDebtResponse settle(@PathVariable Long id, @Valid @RequestBody FinanceDtos.DebtSettlementRequest request,
                                                  org.springframework.security.core.Authentication auth) { return service.settleDebt(id, request, auth.getName()); }

    @GetMapping("/partner-debts/{id}/settlements")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')")
    public java.util.List<FinanceDtos.DebtSettlementResponse> settlements(@PathVariable Long id) { return service.debtSettlementHistory(id); }

    @GetMapping("/ledger")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')")
    public FinanceDtos.PageResponse<FinanceDtos.LedgerEntryResponse> ledger(@RequestParam(required = false) String entryType,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.ledger(entryType, from, to, page, size);
    }

    @GetMapping("/reconciliation")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')")
    public FinanceDtos.ReconciliationResponse reconciliation(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to) { return service.reconcile(from, to); }
}
