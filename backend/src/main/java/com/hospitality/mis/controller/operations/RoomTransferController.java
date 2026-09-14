package com.hospitality.mis.controller.operations;
import com.hospitality.mis.dto.operations.RoomTransferDtos;

import com.hospitality.mis.service.operations.RoomTransferService;
import com.hospitality.mis.middleware.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/**
 * Ghi nhận việc chuyển phòng cho một đặt phòng.
 */
@RestController
@RequestMapping("/api/operations/reservations")
public class RoomTransferController {
    /** Dịch vụ thực hiện chuyển phòng và kiểm tra trạng thái đặt phòng/phòng đích. */
    private final RoomTransferService service;
    public RoomTransferController(RoomTransferService service) { this.service = service; }
    /**
     * Chuyển phòng qua POST /api/operations/reservations/{reservationId}/room-transfers.
     * reservationId là path parameter, body tạo chuyển phòng được {@code @Valid} kiểm tra, header bắt buộc
     * {@code Idempotency-Key} chống chuyển trùng; response là kết quả chuyển phòng.
     * Chỉ RESERVATION_WRITE được phép; xung đột phòng, đặt phòng không hợp lệ hoặc khóa lặp do dịch vụ báo lỗi.
     */
    @PostMapping("/{reservationId}/room-transfers")
    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_WRITE')")
    public RoomTransferDtos.Response transfer(@PathVariable Long reservationId, @Valid @RequestBody RoomTransferDtos.CreateRequest request,
                                              @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.transfer(reservationId, request, SecurityActor.currentActor(), idempotencyKey);
    }
}
