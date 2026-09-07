package com.hospitality.mis.billing.api;

import com.hospitality.mis.billing.application.ServiceCatalogService;
import com.hospitality.mis.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceCatalogService service;

    public ServiceController(ServiceCatalogService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceDtos.Response> findAll() {
        return service.findAll();
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTING')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceDtos.Response create(@Valid @RequestBody ServiceDtos.CreateRequest request) {
        return service.create(request, SecurityActor.currentActor());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTING', 'FRONT_DESK', 'HOUSEKEEPING')")
    @PostMapping("/{id}/stock")
    public ServiceDtos.Response restock(@PathVariable String id,
                                        @Valid @RequestBody ServiceDtos.StockRequest request) {
        return service.restock(id, request, SecurityActor.currentActor());
    }
}
