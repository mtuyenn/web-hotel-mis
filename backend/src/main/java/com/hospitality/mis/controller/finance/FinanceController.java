package com.hospitality.mis.controller.finance;

import com.hospitality.mis.dto.finance.FinanceDtos;
import com.hospitality.mis.service.finance.FinanceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {
    private final FinanceService service;
    public FinanceController(FinanceService service) { this.service = service; }
    @PostMapping("/cash-handovers") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')") public FinanceDtos.CashHandoverResponse handover(@Valid @RequestBody FinanceDtos.CashHandoverRequest request, org.springframework.security.core.Authentication auth) { return service.handover(request, auth.getName()); }
    @PostMapping("/expenses") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')") public FinanceDtos.ExpenseResponse expense(@Valid @RequestBody FinanceDtos.ExpenseRequest request, org.springframework.security.core.Authentication auth) { return service.recordExpense(request, auth.getName()); }
    @PostMapping("/partner-debts") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')") public FinanceDtos.PartnerDebtResponse debt(@Valid @RequestBody FinanceDtos.PartnerDebtRequest request) { return service.recordDebt(request); }
    @GetMapping("/cash-handovers") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')") public java.util.List<FinanceDtos.CashHandoverResponse> handovers() { return service.listHandovers(); }
    @GetMapping("/expenses") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')") public java.util.List<FinanceDtos.ExpenseResponse> expenses() { return service.listExpenses(); }
    @GetMapping("/partner-debts") @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')") public java.util.List<FinanceDtos.PartnerDebtResponse> debts() { return service.listDebts(); }
}
