package com.hospitality.mis.operations.api;
import com.hospitality.mis.operations.application.RoomTransferService;
import com.hospitality.mis.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/operations/reservations")
public class RoomTransferController {
    private final RoomTransferService service;
    public RoomTransferController(RoomTransferService service) { this.service = service; }
    @PostMapping("/{reservationId}/room-transfers") @PreAuthorize("hasAnyRole('MANAGER','FRONT_DESK')")
    public RoomTransferDtos.Response transfer(@PathVariable Long reservationId, @Valid @RequestBody RoomTransferDtos.CreateRequest request) {
        return service.transfer(reservationId, request, SecurityActor.currentActor());
    }
}
