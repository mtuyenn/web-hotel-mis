package com.hospitality.mis.controller.billing;
import com.hospitality.mis.dto.billing.ServiceDtos;




import com.hospitality.mis.service.billing.ServiceCatalogService;

import com.hospitality.mis.middleware.security.SecurityActor;

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


    @PreAuthorize("@departmentAccess.allows(authentication, 'SERVICE_READ')")
    public List<ServiceDtos.Response> findAll() {

        return service.findAll();

    }





    @PostMapping

    @ResponseStatus(HttpStatus.CREATED)

    @PreAuthorize("@departmentAccess.allows(authentication, 'SERVICE_WRITE')")
    public ServiceDtos.Response create(@Valid @RequestBody ServiceDtos.CreateRequest request) {

        return service.create(request, SecurityActor.currentActor());

    }





    @PostMapping("/{id}/stock")


    @PreAuthorize("@departmentAccess.allows(authentication, 'INVENTORY_WRITE')")
    public ServiceDtos.Response restock(@PathVariable String id,

                                        @Valid @RequestBody ServiceDtos.StockRequest request) {

        return service.restock(id, request, SecurityActor.currentActor());

    }

}
