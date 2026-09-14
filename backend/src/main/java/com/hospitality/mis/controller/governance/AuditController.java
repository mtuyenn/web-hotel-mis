package com.hospitality.mis.controller.governance;

import com.hospitality.mis.dto.governance.AuditDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cung cấp nhật ký kiểm toán theo phạm vi mà actor và vai trò toàn cục được phép xem.
 */
@RestController
@RequestMapping("/api/governance/audit")
public class AuditController {
    /** Dịch vụ truy vấn sự kiện kiểm toán theo actor và cờ phạm vi toàn cục. */
    private final AuditService audit;
    public AuditController(AuditService audit) { this.audit = audit; }

    /**
     * Liệt kê nhật ký qua GET /api/governance/audit; không có path/query/header/body tham số.
     * Chỉ AUDIT_READ được gọi; controller truyền actor hiện tại và chỉ bật phạm vi toàn cục cho ADMIN, DIRECTOR hoặc MANAGER.
     * Trả danh sách sự kiện kiểm toán; lỗi truy vấn do dịch vụ xử lý và thao tác đọc không cần idempotency.
     */
    @GetMapping
    
    @PreAuthorize("@departmentAccess.allows(authentication, 'AUDIT_READ')")
    public List<AuditDtos.Response> list() {
        boolean global = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DIRECTOR") || a.getAuthority().equals("ROLE_MANAGER"));
        return audit.list(SecurityActor.currentActor(), global).stream()
                .map(AuditDtos.Response::from)
                .collect(Collectors.toList());
    }
}
