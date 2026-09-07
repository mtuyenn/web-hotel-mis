package com.hospitality.mis.reservation.api;

import com.hospitality.mis.reservation.application.ReservationService;
import com.hospitality.mis.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;
    private final com.hospitality.mis.operations.application.EquipmentIncidentService incidents;

    public ReservationController(ReservationService service,
                                 com.hospitality.mis.operations.application.EquipmentIncidentService incidents) {
        this.service = service;
        this.incidents = incidents;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTING', 'FRONT_DESK')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDtos.Response create(@Valid @RequestBody ReservationDtos.CreateRequest request) {
        return service.create(request, SecurityActor.currentActor());
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public ReservationDtos.Response get(@PathVariable Long id) {
        return service.get(id);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'FRONT_DESK')")
    @PostMapping("/{id}/check-in")
    public ReservationDtos.Response checkIn(@PathVariable Long id,
                                            @RequestBody(required = false) ReservationDtos.CheckInRequest request) {
        return service.checkIn(id, request, SecurityActor.currentActor());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTING', 'FRONT_DESK')")
    @PostMapping("/{id}/check-out")
    public com.hospitality.mis.billing.api.InvoiceDtos.Response checkOut(
            @PathVariable Long id, @Valid @RequestBody ReservationDtos.CheckOutRequest request) {
        return service.checkOut(id, request, SecurityActor.currentActor());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'FRONT_DESK')")
    @PostMapping("/{id}/cancel")
    public ReservationDtos.Response cancel(@PathVariable Long id) {
        return service.cancel(id, SecurityActor.currentActor());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'FRONT_DESK')")
    @PostMapping("/{id}/extend")
    public ReservationDtos.Response extend(@PathVariable Long id,
                                           @Valid @RequestBody ReservationDtos.ExtendRequest request) {
        return service.extend(id, request, SecurityActor.currentActor());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'FRONT_DESK', 'HOUSEKEEPING')")
    @PostMapping("/{id}/services")
    public ReservationDtos.Response addService(@PathVariable Long id,
                                               @Valid @RequestBody ReservationDtos.AddServiceRequest request) {
        return service.addService(id, request, SecurityActor.currentActor());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'FRONT_DESK', 'HOUSEKEEPING')")
    @PostMapping("/{id}/equipment-incidents")
    public com.hospitality.mis.operations.api.EquipmentIncidentDtos.Response equipmentIncident(
            @PathVariable Long id,
            @Valid @RequestBody com.hospitality.mis.operations.api.EquipmentIncidentDtos.CreateRequest request) {
        return incidents.record(id, request, SecurityActor.currentActor());
    }
}
