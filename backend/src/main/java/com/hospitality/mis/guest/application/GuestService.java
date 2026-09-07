package com.hospitality.mis.guest.application;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.guest.api.GuestDtos;
import com.hospitality.mis.guest.domain.Guest;
import com.hospitality.mis.guest.domain.MembershipTier;
import com.hospitality.mis.governance.application.AuditService;
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
        Guest guest = guests.newGuest();
        guest.setFullName(required(request.fullName(), "fullName"));
        guest.setBirthYear(request.birthYear());
        guest.setIdentityNumber(required(request.identityNumber(), "identityNumber"));
        guest.setPhone(required(request.phone(), "phone"));
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
        return guests.findById(id).map(this::toResponse)
                .orElseThrow(() -> new DomainException("GUEST_NOT_FOUND", "Không tìm thấy khách hàng: " + id));
    }

    @Transactional(readOnly = true)
    public List<GuestDtos.Response> search(String query) {
        if (query == null || query.isBlank()) return guests.findAll().stream().map(this::toResponse).toList();
        return guests.search(query.trim()).stream().map(this::toResponse).toList();
    }

    public GuestDtos.Response toResponse(Guest g) {
        return new GuestDtos.Response(g.getId(), g.getFullName(), g.getBirthYear(), g.getIdentityNumber(), g.getPhone(),
                g.getEmail(), g.getAddress(), g.getMembershipTier(), g.getTotalSpend(), g.getLateCancellationCount(),
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
