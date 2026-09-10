package com.hospitality.mis.controller.room;
import com.hospitality.mis.dto.room.RoomDtos;




import com.hospitality.mis.common.exception.DomainException;

import com.hospitality.mis.service.room.RoomService;

import com.hospitality.mis.entity.room.RoomStatus;

import com.hospitality.mis.middleware.security.SecurityActor;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



import java.time.LocalDateTime;

import java.util.List;



@RestController

@RequestMapping("/api/rooms")

public class RoomController {

    private final RoomService service;



    public RoomController(RoomService service) {

        this.service = service;

    }



    @GetMapping


    @PreAuthorize("@departmentAccess.allows(authentication, 'ROOM_READ')")
    public List<RoomDtos.Response> search(@RequestParam(required = false) String type,

                                          @RequestParam(required = false) String status) {

        return service.search(type, parseStatus(status));

    }



    @GetMapping("/availability")


    @PreAuthorize("@departmentAccess.allows(authentication, 'ROOM_READ')")
    public List<RoomDtos.Availability> availability(

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @RequestParam(required = false) String type) {

        return service.availability(from, to, type);

    }





    @PatchMapping("/{id}/status")


    @PreAuthorize("@departmentAccess.allows(authentication, 'ROOM_WRITE')")
    public RoomDtos.Response updateStatus(@PathVariable String id, @RequestParam String status) {

        return service.updateStatus(id, parseStatus(status), SecurityActor.currentActor());

    }



    private RoomStatus parseStatus(String value) {

        if (value == null) {

            return null;

        }

        try {

            return RoomStatus.fromApiValue(value);
        } catch (IllegalArgumentException exception) {

            throw new DomainException("INVALID_ROOM_STATUS", "Trạng thái phòng không hợp lệ: " + value);

        }

    }

}
