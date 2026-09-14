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
import com.hospitality.mis.service.governance.DurableIdempotencyService;
import com.hospitality.mis.service.reservation.IdempotencySupport;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Clock;

/** Ghi sự cố thiết bị trong phòng đang có khách và tính khoản bồi thường. */
@Service
public class EquipmentIncidentService {
    /** Kho sự cố, lưu giá trị bồi thường đã tính tại thời điểm ghi nhận. */
    private final EquipmentIncidentRepository incidents;
    /** Khóa đặt phòng để xác nhận khách đang ở và phòng thuộc đặt phòng. */
    private final ReservationRepository reservations;
    /** Policy tính bồi thường theo tuổi và giá trị thiết bị. */
    private final PricingPolicy pricing;
    /** Ghi audit sự cố và số tiền bồi thường. */
    private final AuditService audit;
    /** Cache kết quả hoàn tất trong instance để retry cùng actor/payload không ghi trùng. */
    private final IdempotencySupport idempotency = new IdempotencySupport();
    private DurableIdempotencyService durableIdempotency;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

    public EquipmentIncidentService(EquipmentIncidentRepository incidents, ReservationRepository reservations,
                                    PricingPolicy pricing, AuditService audit) {
        this.incidents = incidents;
        this.reservations = reservations;
        this.pricing = pricing;
        this.audit = audit;
    }

    @org.springframework.beans.factory.annotation.Autowired
    void setDurableIdempotency(DurableIdempotencyService durableIdempotency) { this.durableIdempotency = durableIdempotency; }

    @org.springframework.beans.factory.annotation.Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }

    /** Khóa đặt phòng, xác nhận room occupied, tính bồi thường và ghi sự cố idempotent. */
    @Transactional
    public EquipmentIncidentDtos.Response record(Long reservationId, EquipmentIncidentDtos.CreateRequest request,
                                                 String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        if (request == null) throw new DomainException("INVALID_REQUEST", "Thiếu nội dung báo sự cố");
        String fingerprint = IdempotencySupport.fingerprint("EQUIPMENT_INCIDENT|" + reservationId + "|" + request.roomId()
                + "|" + request.equipmentName() + "|" + request.originalValue() + "|" + request.purchasedAt()
                + "|" + request.quantity());
        return executeIdempotent("equipment-incident", key, actor, fingerprint, EquipmentIncidentDtos.Response.class, () -> {
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
                    request.quantity(), LocalDate.now(clock));
            var incidentEntity = new EquipmentIncident(reservation, room, request.equipmentName(),
                    request.originalValue(), request.purchasedAt(), request.quantity(), amount);
            incidentEntity.setCreatedAt(java.time.LocalDateTime.now(clock));
            var incident = incidents.save(incidentEntity);
            audit.record(actor, "EQUIPMENT_INCIDENT_RECORDED", "RESERVATION", reservationId.toString(), null,
                    amount.toPlainString(), null);
            return new EquipmentIncidentDtos.Response(incident.getId(), request.roomId(), request.equipmentName(), amount);
        });
    }

    private <T> T executeIdempotent(String scope, String key, String actor, String fingerprint,
                                    Class<T> responseType, java.util.function.Supplier<T> command) {
        return durableIdempotency == null
                ? idempotency.execute(scope, key, actor, fingerprint, command)
                : durableIdempotency.execute(scope, key, actor, fingerprint, responseType, command);
    }

    /** Ràng buộc actor được truyền vào với principal hiện tại, chuẩn hóa lỗi bảo mật. */
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
