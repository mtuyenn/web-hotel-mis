package com.hospitality.mis.controller.reservation;
import com.hospitality.mis.dto.billing.InvoiceDtos;

import com.hospitality.mis.dto.operations.EquipmentIncidentDtos;

import com.hospitality.mis.dto.reservation.ReservationDtos;

import com.hospitality.mis.service.operations.EquipmentIncidentService;




import com.hospitality.mis.service.reservation.ReservationService;

import com.hospitality.mis.middleware.security.SecurityActor;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.bind.annotation.RestController;



@RestController

@RequestMapping("/api/reservations")

public class ReservationController {

    private final ReservationService service;

    private final com.hospitality.mis.service.operations.EquipmentIncidentService incidents;



    public ReservationController(ReservationService service,

                                 com.hospitality.mis.service.operations.EquipmentIncidentService incidents) {

        this.service = service;

        this.incidents = incidents;

    }


    @org.springframework.web.bind.annotation.GetMapping
    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_READ')")
    public ReservationDtos.PageResponse list(
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.hospitality.mis.entity.reservation.ReservationStatus status,
            @org.springframework.web.bind.annotation.RequestParam(name = "guest_id", required = false) Long guestId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return service.list(status, guestId, page, size);
    }





    @PostMapping

    @ResponseStatus(HttpStatus.CREATED)

    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_CREATE')")
    public ReservationDtos.Response create(@Valid @RequestBody ReservationDtos.CreateRequest request,
                                           @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        return service.create(request, SecurityActor.currentActor(), idempotencyKey);

    }




    @org.springframework.web.bind.annotation.GetMapping("/{id}")

    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_READ')")
    public ReservationDtos.Response get(@PathVariable Long id) {
        ReservationDtos.Response response = service.get(id);
        String actor = SecurityActor.currentActor();
        if (!actor.equals(response.employeeId()) && !isGlobalReadRole()) {
            throw new AccessDeniedException("Không được phép xem đặt phòng ngoài phạm vi");
        }
        return response;
    }

    private boolean isGlobalReadRole() {
        return com.hospitality.mis.middleware.security.ReservationAccess.hasGlobalRead();
    }




    @PostMapping("/{id}/check-in")


    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_WRITE')")
    public ReservationDtos.Response checkIn(@PathVariable Long id,

                                            @RequestBody(required = false) ReservationDtos.CheckInRequest request,
                                            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return service.checkIn(id, request, SecurityActor.currentActor(), idempotencyKey);

    }





    @PostMapping("/{id}/check-out")


    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_CHECKOUT')")
    public com.hospitality.mis.dto.billing.InvoiceDtos.Response checkOut(

            @PathVariable Long id, @Valid @RequestBody ReservationDtos.CheckOutRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return service.checkOut(id, request, SecurityActor.currentActor(), idempotencyKey);

    }





    @PostMapping("/{id}/cancel")


    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_WRITE')")
    public ReservationDtos.Response cancel(@PathVariable Long id, @Valid @RequestBody ReservationDtos.CancelRequest request,
                                           @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return service.cancel(id, request, SecurityActor.currentActor(), idempotencyKey);

    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_WRITE')")
    public ReservationDtos.Response noShow(@PathVariable Long id,
                                           @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.markNoShow(id, SecurityActor.currentActor(), idempotencyKey);
    }





    @PostMapping("/{id}/extend")


    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_WRITE')")
    public ReservationDtos.Response extend(@PathVariable Long id,

                                           @Valid @RequestBody ReservationDtos.ExtendRequest request,
                                           @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return service.extend(id, request, SecurityActor.currentActor(), idempotencyKey);

    }





    @PostMapping("/{id}/services")


    @PreAuthorize("@departmentAccess.allows(authentication, 'RESERVATION_SERVICE_WRITE')")
    public ReservationDtos.Response addService(@PathVariable Long id,

                                               @Valid @RequestBody ReservationDtos.AddServiceRequest request,
                                               @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return service.addService(id, request, SecurityActor.currentActor(), idempotencyKey);

    }





    @PostMapping("/{id}/equipment-incidents")


    @PreAuthorize("@departmentAccess.allows(authentication, 'INCIDENT_WRITE')")
    public com.hospitality.mis.dto.operations.EquipmentIncidentDtos.Response equipmentIncident(

            @PathVariable Long id,

            @Valid @RequestBody com.hospitality.mis.dto.operations.EquipmentIncidentDtos.CreateRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return incidents.record(id, request, SecurityActor.currentActor(), idempotencyKey);

    }

}
