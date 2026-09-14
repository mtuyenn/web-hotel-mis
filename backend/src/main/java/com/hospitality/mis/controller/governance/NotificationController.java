package com.hospitality.mis.controller.governance;

import com.hospitality.mis.dto.governance.NotificationDtos;
import com.hospitality.mis.service.governance.NotificationOutboxService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/governance/notifications")
public class NotificationController {
    private final NotificationOutboxService service;
    public NotificationController(NotificationOutboxService service) { this.service = service; }
    @GetMapping("/outbox")
    @PreAuthorize("@departmentAccess.allows(authentication, 'NOTIFICATION_READ')")
    public List<NotificationDtos.Response> poll(@RequestParam(required = false) String role) { return service.poll(role); }
    @PostMapping("/outbox/{id}/delivered")
    @PreAuthorize("@departmentAccess.allows(authentication, 'NOTIFICATION_WRITE')")
    public NotificationDtos.Response delivered(@PathVariable Long id) { return service.markDelivered(id); }
}
