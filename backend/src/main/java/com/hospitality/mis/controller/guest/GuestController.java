package com.hospitality.mis.controller.guest;
import com.hospitality.mis.dto.guest.GuestDtos;




import com.hospitality.mis.service.guest.GuestService;

import com.hospitality.mis.middleware.security.SecurityActor;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



@RestController

@RequestMapping("/api/guests")

public class GuestController {

    private final GuestService service;



    public GuestController(GuestService service) {

        this.service = service;

    }




    @GetMapping
    @PreAuthorize("@departmentAccess.allows(authentication, 'GUEST_READ')")
    public List<GuestDtos.Response> search(@RequestParam(required = false) String q) {
        return service.search(q);
    }


    @GetMapping("/{id}")

    @PreAuthorize("@departmentAccess.allows(authentication, 'GUEST_READ')")
    public GuestDtos.Response get(@PathVariable Long id) {
        return service.get(id);

    }





    @PostMapping

    @ResponseStatus(HttpStatus.CREATED)

    @PreAuthorize("@departmentAccess.allows(authentication, 'GUEST_WRITE')")
    public GuestDtos.Response create(@Valid @RequestBody GuestDtos.CreateRequest request) {

        return service.create(request, SecurityActor.currentActor());

    }

}
