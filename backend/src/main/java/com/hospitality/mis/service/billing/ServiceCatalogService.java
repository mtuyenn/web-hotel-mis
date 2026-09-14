package com.hospitality.mis.service.billing;



import com.hospitality.mis.dao.billing.ServiceRepository;

import com.hospitality.mis.dto.billing.ServiceDtos;

import com.hospitality.mis.common.exception.DomainException;

import com.hospitality.mis.service.governance.AuditService;

import com.hospitality.mis.entity.billing.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;



/** Quản lý danh mục dịch vụ, giá bán và tồn kho dịch vụ dùng cho đặt phòng. */
@org.springframework.stereotype.Service
public class ServiceCatalogService {

    /** Kho dịch vụ (restock dùng khóa) và cộng tác viên audit cho mọi thay đổi danh mục. */
    private final ServiceRepository services; private final AuditService audit;

    public ServiceCatalogService(ServiceRepository services, AuditService audit) { this.services = services; this.audit = audit; }

    @Transactional(readOnly = true)

    /** Trả toàn bộ danh mục dịch vụ dưới dạng DTO. */
    public List<ServiceDtos.Response> findAll() { return services.findAll().stream().map(this::toResponse).toList(); }

    @Transactional

    /** Tạo dịch vụ mới, khởi tạo tồn kho và ngưỡng cảnh báo, rồi ghi audit. */
    public ServiceDtos.Response create(ServiceDtos.CreateRequest request, String actor) {

        if (services.existsById(request.id())) throw new DomainException("SERVICE_EXISTS", "Mã dịch vụ đã tồn tại");

        var service = new Service(); service.setId(request.id()); service.setName(request.name()); service.setPrice(request.price());
        service.setUnit(request.unit() == null || request.unit().isBlank() ? "LẦN" : request.unit());
        service.setStockQuantity(request.openingStock()); service.setSafetyThreshold(request.safetyThreshold()); services.save(service);
        audit.record(actor, "SERVICE_CREATED", "SERVICE", request.id(), null, request.name(), null); return toResponse(service);

    }

    @Transactional

    /** Khóa dịch vụ, cộng tồn kho nhập thêm và ghi actor thực hiện. */
    public ServiceDtos.Response restock(String id, ServiceDtos.StockRequest request, String actor) {

        var service = services.findWithLockById(id).orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
        service.setStockQuantity(service.getStockQuantity() + request.quantity());
        audit.record(actor, "SERVICE_RESTOCKED", "SERVICE", id, null, String.valueOf(request.quantity()), null); return toResponse(service);

    }

    /** Chuyển dịch vụ thành DTO và tính cờ tồn kho dưới ngưỡng an toàn. */
    /** Chuyển dịch vụ thành DTO và tính cờ cảnh báo dưới safety threshold. */
    public ServiceDtos.Response toResponse(Service s) { return new ServiceDtos.Response(s.getId(), s.getName(), s.getPrice(), s.getUnit(), s.getStockQuantity(), s.getSafetyThreshold(), s.getStockQuantity() < s.getSafetyThreshold()); }
}
