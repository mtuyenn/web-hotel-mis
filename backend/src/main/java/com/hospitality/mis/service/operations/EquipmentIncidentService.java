package com.hospitality.mis.service.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dto.operations.EquipmentIncidentDtos;
import com.hospitality.mis.entity.operations.EquipmentIncident;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.billing.PricingPolicy;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.reservation.IdempotencySupport;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EquipmentIncidentService {
    private final EquipmentIncidentRepository incidents;
    private final ReservationRepository reservations;
    private final PricingPolicy pricing;
    private final AuditService audit;
    private final IdempotencySupport idempotency = new IdempotencySupport();

    public EquipmentIncidentService(EquipmentIncidentRepository incidents, ReservationRepository reservations,
                                    PricingPolicy pricing, AuditService audit) {
        this.incidents = incidents;
        this.reservations = reservations;
        this.pricing = pricing;
        this.audit = audit;
    }

    @Transactional
    public EquipmentIncidentDtos.Response record(Long reservationId, EquipmentIncidentDtos.CreateRequest request,
                                                 String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        if (request == null) throw new DomainException("INVALID_REQUEST", "Thiếu nội dung báo sự cố");
        String fingerprint = IdempotencySupport.fingerprint("EQUIPMENT_INCIDENT|" + reservationId + "|" + request.roomId()
                + "|" + request.equipmentName() + "|" + request.originalValue() + "|" + request.purchasedAt()
                + "|" + request.quantity());
        return idempotency.execute("equipment-incident", key, actor, fingerprint, () -> {
            var reservation = reservations.findForUpdate(reservationId)
                    .orElseThrow(() -> new DomainException("RESERVATION_NOT_FOUND", "Không tìm thấy đặt phòng"));
            if (reservation.getStatus() != ReservationStatus.CHECKED_IN)
                throw new DomainException("INVALID_STATE", "Chỉ ghi nhận sự cố khi khách đang ở");
            var room = reservation.getRooms().stream()
                    .filter(line -> line.getStatus() == RoomStatus.OCCUPIED)
                    .map(line -> line.getRoom())
                    .filter(candidate -> candidate.getId().equals(request.roomId()))
                    .findFirst()
                    .orElseThrow(() -> new DomainException("ROOM_NOT_IN_RESERVATION",
                            "Phòng không thuộc đặt phòng này"));
            var amount = pricing.equipmentCompensation(request.originalValue(), request.purchasedAt(),
                    request.quantity(), LocalDate.now());
            var incident = incidents.save(new EquipmentIncident(reservation, room, request.equipmentName(),
                    request.originalValue(), request.purchasedAt(), request.quantity(), amount));
            audit.record(actor, "EQUIPMENT_INCIDENT_RECORDED", "RESERVATION", reservationId.toString(), null,
                    amount.toPlainString(), null);
            return new EquipmentIncidentDtos.Response(incident.getId(), request.roomId(), request.equipmentName(), amount);
        });
    }

    private String authenticatedActor(String supplied) {
        try {
            return SecurityActor.requireBoundActor(supplied);
        } catch (AuthenticationCredentialsNotFoundException exception) {
            throw new DomainException("ACTOR_REQUIRED", "Thiếu actor đã xác thực");
        } catch (AccessDeniedException exception) {
            throw new DomainException("ACTOR_MISMATCH", "Actor không khớp principal hiện tại");
        }
    }
}
