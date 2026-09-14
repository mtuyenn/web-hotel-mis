package com.hospitality.mis.controller.operations;
import com.hospitality.mis.dto.operations.MaintenanceDtos;


import com.hospitality.mis.service.operations.MaintenanceService;
import com.hospitality.mis.middleware.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Theo dõi sự cố bảo trì theo phòng và chuyển trạng thái phiếu bảo trì.
 */
@RestController
@RequestMapping("/api/operations/maintenance")
public class MaintenanceController {
    /** Dịch vụ truy vấn, tạo phiếu và cập nhật trạng thái bảo trì. */
    private final MaintenanceService service;
    public MaintenanceController(MaintenanceService service) { this.service = service; }
    /**
     * Liệt kê bảo trì của phòng qua GET /api/operations/maintenance/room/{roomId}; roomId là path parameter.
     * Trả danh sách phiếu, chỉ MAINTENANCE_READ được phép; lỗi phòng hoặc truy vấn do dịch vụ xử lý và không cần idempotency.
     */
    @GetMapping("/room/{roomId}")
    @PreAuthorize("@departmentAccess.allows(authentication, 'MAINTENANCE_READ')")
    public List<MaintenanceDtos.Response> byRoom(@PathVariable String roomId) { return service.byRoom(roomId); }
    /**
     * Tạo phiếu bảo trì qua POST /api/operations/maintenance; body được {@code @Valid} kiểm tra, actor hiện tại được truyền,
     * response là phiếu mới. Chỉ MAINTENANCE_WRITE được phép; không có idempotency key, còn lỗi dữ liệu/trạng thái do dịch vụ báo.
     */
    @PostMapping
    @PreAuthorize("@departmentAccess.allows(authentication, 'MAINTENANCE_WRITE')")
    public MaintenanceDtos.Response create(@Valid @RequestBody MaintenanceDtos.CreateRequest request) { return service.create(request, SecurityActor.currentActor()); }
    /**
     * Cập nhật trạng thái phiếu qua PATCH /api/operations/maintenance/{id}/status.
     * id là path parameter, body trạng thái được {@code @Valid} kiểm tra, actor được ghi nhận; trả phiếu sau cập nhật.
     * Chỉ MAINTENANCE_WRITE được phép; chuyển trạng thái không hợp lệ hoặc phiếu không tồn tại tạo lỗi nghiệp vụ.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("@departmentAccess.allows(authentication, 'MAINTENANCE_WRITE')")
    public MaintenanceDtos.Response status(@PathVariable String id, @Valid @RequestBody MaintenanceDtos.StatusRequest request) { return service.updateStatus(id, request, SecurityActor.currentActor()); }
}
