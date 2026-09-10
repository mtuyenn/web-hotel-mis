package com.hospitality.mis.controller.operations;
import com.hospitality.mis.dto.operations.RoomTransferDtos;

import com.hospitality.mis.service.operations.RoomTransferService;
import com.hospitality.mis.middleware.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/operations/reservations")
public class RoomTransferController {
    private final RoomTransferService service;
    public RoomTransferController(RoomTransferService service) { this.service = service; }
    @PostMapping("/{reservationId}/room-transfers")
    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_WRITE')")
    public RoomTransferDtos.Response transfer(@PathVariable Long reservationId, @Valid @RequestBody RoomTransferDtos.CreateRequest request,
                                              @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.transfer(reservationId, request, SecurityActor.currentActor(), idempotencyKey);
    }
}
