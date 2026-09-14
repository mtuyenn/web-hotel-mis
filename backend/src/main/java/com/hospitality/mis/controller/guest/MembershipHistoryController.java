package com.hospitality.mis.controller.guest;

import com.hospitality.mis.dto.guest.MembershipHistoryDtos;
import com.hospitality.mis.service.guest.MembershipHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Cung cấp lịch sử thay đổi hạng thành viên của một khách.
 */
@RestController
@RequestMapping("/api/guests/{guestId}/membership-history")
public class MembershipHistoryController {
    /** Dịch vụ truy vấn lịch sử thành viên theo guestId. */
    private final MembershipHistoryService service;
    public MembershipHistoryController(MembershipHistoryService service) { this.service = service; }
    /**
     * Liệt kê lịch sử thành viên qua GET /api/guests/{guestId}/membership-history.
     * guestId là path parameter, không có body; trả danh sách biến động hạng để hiển thị và đối soát.
     * Chỉ GUEST_READ được phép; khách không tồn tại hoặc lỗi truy vấn do dịch vụ xử lý, thao tác đọc không cần idempotency.
     */
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'GUEST_READ')")
    public List<MembershipHistoryDtos.Response> list(@PathVariable Long guestId) { return service.list(guestId); }
}
