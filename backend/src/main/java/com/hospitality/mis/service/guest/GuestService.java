package com.hospitality.mis.service.guest;
import com.hospitality.mis.dao.guest.GuestStore;






import com.hospitality.mis.common.exception.DomainException;

import com.hospitality.mis.dto.guest.GuestDtos;

import com.hospitality.mis.entity.guest.Guest;

import com.hospitality.mis.entity.guest.MembershipTier;

import com.hospitality.mis.service.governance.AuditService;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.math.BigDecimal;

import java.util.List;



@Service

public class GuestService {

    private final GuestStore guests;

    private final AuditService audit;



    public GuestService(GuestStore guests, AuditService audit) {

        this.guests = guests;

        this.audit = audit;

    }



    @Transactional

    public GuestDtos.Response create(GuestDtos.CreateRequest request, String actor) {

        String identityNumber = required(request.identityNumber(), "identityNumber");
        String phone = required(request.phone(), "phone");
        if (guests.findByPhone(phone).isPresent()) {
            throw new DomainException("GUEST_PHONE_EXISTS", "Số điện thoại khách hàng đã tồn tại");
        }
        if (guests.findByIdentityNumber(identityNumber).isPresent()) {
            throw new DomainException("GUEST_IDENTITY_EXISTS", "Số giấy tờ khách hàng đã tồn tại");
        }

        Guest guest = guests.newGuest();

        guest.setFullName(required(request.fullName(), "fullName"));

        guest.setBirthYear(request.birthYear());

        guest.setIdentityNumber(identityNumber);

        guest.setPhone(phone);

        guest.setEmail(optional(request.email()));

        guest.setAddress(optional(request.address()));

        guest.setMembershipTier(MembershipTier.STANDARD);

        guest.setTotalSpend(BigDecimal.ZERO);

        Guest saved = guests.save(guest);

        audit.record(actor, "GUEST_CREATED", "GUEST", saved.getId().toString(), null, saved.getFullName(), null);

        return toResponse(saved);

    }



    @Transactional(readOnly = true)
    public GuestDtos.Response get(Long id) {
        return guests.findSharedById(id).map(this::toResponse)
                .orElseThrow(() -> new DomainException("GUEST_NOT_FOUND", "Không tìm thấy khách hàng: " + id));
    }

    @Transactional(readOnly = true)
    public List<GuestDtos.Response> search(String query) {
        if (query == null || query.isBlank()) {
            return guests.findAllShared().stream().map(this::toResponse).toList();
        }
        return guests.searchShared(query.trim()).stream().map(this::toResponse).toList();
    }


    public GuestDtos.Response toResponse(Guest g) {

        return new GuestDtos.Response(g.getId(), g.getFullName(), g.getBirthYear(), g.getIdentityNumber(), g.getPhone(),

                g.getEmail(), g.getAddress(), g.getMembershipTier(), g.getTotalSpend(), g.getLateCancellationCount(),
                g.getLateCheckoutCount(),

                g.isBookingBlocked());

    }



    private static String required(String value, String field) {

        if (value == null || value.isBlank()) {

            throw new DomainException("" + field.toUpperCase() + "_REQUIRED", "Trường " + field + " là bắt buộc");

        }

        return value.trim();

    }



    private static String optional(String value) {

        return value == null || value.isBlank() ? null : value.trim();

    }

}
