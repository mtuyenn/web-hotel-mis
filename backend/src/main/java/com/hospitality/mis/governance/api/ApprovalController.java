package com.hospitality.mis.governance.api;

import com.hospitality.mis.governance.application.ApprovalService;
import com.hospitality.mis.security.SecurityActor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

final class ApprovalDtos {
    private ApprovalDtos() {
    }

    public record Request(@NotBlank String action, @NotBlank String targetId, @NotBlank String reason) {
    }
}

@RestController
@RequestMapping("/api/governance/approvals")
class ApprovalController {
    private final ApprovalService service;

    ApprovalController(ApprovalService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTING', 'FRONT_DESK')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public com.hospitality.mis.governance.domain.ApprovalRequest request(
            @RequestBody @Valid ApprovalDtos.Request request) {
        return service.request(SecurityActor.currentActor(), request.action(), request.targetId(), request.reason());
    }

    @PreAuthorize("hasRole('MANAGER') and @approvalAuthorization.canApprove(#id, authentication.name)")
    @PostMapping("/{id}/approve")
    public com.hospitality.mis.governance.domain.ApprovalRequest approve(@PathVariable Long id) {
        return service.approve(id, SecurityActor.currentActor());
    }
}
