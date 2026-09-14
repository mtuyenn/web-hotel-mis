package com.hospitality.mis.controller.room;

import com.hospitality.mis.dto.room.RoomEquipmentDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.room.RoomEquipmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Tra cứu và gắn thiết bị vào một phòng.
 */
@RestController
@RequestMapping("/api/rooms/{roomId}/equipment")
public class RoomEquipmentController {
    /** Dịch vụ đọc và thêm thiết bị, bao gồm kiểm tra quan hệ phòng-thiết bị. */
    private final RoomEquipmentService service;
    public RoomEquipmentController(RoomEquipmentService service) { this.service = service; }
    /**
     * Liệt kê thiết bị của phòng qua GET /api/rooms/{roomId}/equipment; roomId là path parameter.
     * Trả danh sách thiết bị, chỉ EQUIPMENT_READ được phép; lỗi phòng/truy vấn do dịch vụ xử lý và không cần idempotency.
     */
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'EQUIPMENT_READ')")
    public List<RoomEquipmentDtos.Response> list(@PathVariable String roomId) { return service.list(roomId); }
    /**
     * Thêm thiết bị vào phòng qua POST /api/rooms/{roomId}/equipment.
     * roomId là path parameter, body được {@code @Valid} kiểm tra và phải có room_id trùng path; mismatch bị từ chối trước khi gọi service.
     * Chỉ EQUIPMENT_WRITE được phép, trả thiết bị đã thêm; Idempotency-Key bắt buộc và được truyền nguyên vẹn cho service.
     */
    @PostMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'EQUIPMENT_WRITE')")
    public RoomEquipmentDtos.Response add(@PathVariable String roomId, @Valid @RequestBody RoomEquipmentDtos.CreateRequest request,
                                          @RequestHeader("Idempotency-Key") String idempotencyKey) {
        if (!roomId.equals(request.roomId())) {
            throw new com.hospitality.mis.common.exception.DomainException(
                    "ROOM_PATH_MISMATCH", "room_id trong URL và nội dung request phải giống nhau");
        }
        return service.add(request, SecurityActor.currentActor(), idempotencyKey);
    }
}
