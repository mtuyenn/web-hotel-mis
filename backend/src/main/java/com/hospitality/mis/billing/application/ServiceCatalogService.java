package com.hospitality.mis.billing.application;

import com.hospitality.mis.billing.adapter.ServiceRepository;
import com.hospitality.mis.billing.api.ServiceDtos;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.application.AuditService;
import com.hospitality.mis.billing.domain.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceCatalogService {
    private final ServiceRepository services; private final AuditService audit;
    public ServiceCatalogService(ServiceRepository services, AuditService audit) { this.services = services; this.audit = audit; }
    @Transactional(readOnly = true)
    public List<ServiceDtos.Response> findAll() { return services.findAll().stream().map(this::toResponse).toList(); }
    @Transactional
    public ServiceDtos.Response create(ServiceDtos.CreateRequest request, String actor) {
        if (services.existsById(request.id())) throw new DomainException("SERVICE_EXISTS", "Mã dịch vụ đã tồn tại");
        var service = new Service(); service.setId(request.id()); service.setName(request.name()); service.setPrice(request.price());
        service.setUnit(request.unit() == null || request.unit().isBlank() ? "LẦN" : request.unit());
        service.setStockQuantity(request.openingStock()); service.setSafetyThreshold(request.safetyThreshold()); services.save(service);
        audit.record(actor, "SERVICE_CREATED", "SERVICE", request.id(), null, request.name(), null); return toResponse(service);
    }
    @Transactional
    public ServiceDtos.Response restock(String id, ServiceDtos.StockRequest request, String actor) {
        var service = services.findWithLockById(id).orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
        service.setStockQuantity(service.getStockQuantity() + request.quantity());
        audit.record(actor, "SERVICE_RESTOCKED", "SERVICE", id, null, String.valueOf(request.quantity()), null); return toResponse(service);
    }
    public ServiceDtos.Response toResponse(Service s) { return new ServiceDtos.Response(s.getId(), s.getName(), s.getPrice(), s.getUnit(), s.getStockQuantity(), s.getSafetyThreshold(), s.getStockQuantity() < s.getSafetyThreshold()); }
}
