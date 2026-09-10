package com.hospitality.mis.controller.auth;

import com.hospitality.mis.dto.auth.CustomerAccountDtos;
import com.hospitality.mis.service.auth.CustomerAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.hospitality.mis.middleware.security.SecurityActor;

@RestController
@RequestMapping("/api/auth/customers")
public class CustomerAccountController {
    private final CustomerAccountService service;
    public CustomerAccountController(CustomerAccountService service) { this.service = service; }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerAccountDtos.Response register(@Valid @RequestBody CustomerAccountDtos.RegisterRequest request) {
        return service.register(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerAccountDtos.MeResponse me() {
        SecurityActor.Principal actor = SecurityActor.currentPrincipal();
        if (!actor.isCustomer()) throw new org.springframework.security.access.AccessDeniedException("Customer principal required");
        return service.me(Long.valueOf(actor.id()));
    }
}
