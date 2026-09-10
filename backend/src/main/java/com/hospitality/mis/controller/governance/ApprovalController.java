package com.hospitality.mis.controller.governance;

import com.hospitality.mis.dto.governance.ApprovalDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.governance.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/governance/approvals")
class ApprovalController {
    private final ApprovalService service;

    ApprovalController(ApprovalService service) {
        this.service = service;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@departmentAccess.allows(authentication, 'APPROVAL_REQUEST')")
    public ApprovalDtos.Response request(@RequestBody @Valid ApprovalDtos.Request request) {
        return ApprovalDtos.Response.from(service.request(SecurityActor.currentActor(), request.action(),
                request.targetId(), request.payload(), request.amount(), request.reason(), request.idempotencyKey()));
    }


    @PostMapping("/{id}/approve")

    @PreAuthorize("@departmentAccess.allows(authentication, 'APPROVAL_APPROVE') and @approvalAuthorization.canApprove(#id, authentication.name)")
    public ApprovalDtos.Response approve(@PathVariable Long id) {
        return ApprovalDtos.Response.from(service.approve(id, SecurityActor.currentActor()));
    }


    @PostMapping("/{id}/reject")

    @PreAuthorize("@departmentAccess.allows(authentication, 'APPROVAL_APPROVE') and @approvalAuthorization.canApprove(#id, authentication.name)")
    public ApprovalDtos.Response reject(@PathVariable Long id) {
        return ApprovalDtos.Response.from(service.reject(id, SecurityActor.currentActor()));
    }


    @GetMapping
    @PreAuthorize("@departmentAccess.allows(authentication, 'APPROVAL_APPROVE')")
    public List<ApprovalDtos.Response> list(@RequestParam(required = false) String status) {
        return service.list(status).stream().map(ApprovalDtos.Response::from).collect(Collectors.toList());
    }
}
