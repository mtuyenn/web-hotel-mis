package com.hospitality.mis.controller.guest;

import com.hospitality.mis.dto.guest.MembershipHistoryDtos;
import com.hospitality.mis.service.guest.MembershipHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/guests/{guestId}/membership-history")
public class MembershipHistoryController {
    private final MembershipHistoryService service;
    public MembershipHistoryController(MembershipHistoryService service) { this.service = service; }
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'GUEST_READ')")
    public List<MembershipHistoryDtos.Response> list(@PathVariable Long guestId) { return service.list(guestId); }
}
