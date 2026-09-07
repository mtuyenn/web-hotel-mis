package com.hospitality.mis.operations.application;

import com.hospitality.mis.billing.application.PricingPolicy;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.application.AuditService;
import com.hospitality.mis.operations.adapter.EquipmentIncidentRepository;
import com.hospitality.mis.operations.api.EquipmentIncidentDtos;
import com.hospitality.mis.operations.domain.EquipmentIncident;
import com.hospitality.mis.reservation.adapter.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class EquipmentIncidentService {
    private final EquipmentIncidentRepository incidents; private final ReservationRepository reservations;
    private final PricingPolicy pricing; private final AuditService audit;
    public EquipmentIncidentService(EquipmentIncidentRepository incidents, ReservationRepository reservations,
                                    PricingPolicy pricing, AuditService audit) {
        this.incidents = incidents; this.reservations = reservations; this.pricing = pricing; this.audit = audit;
    }
    @Transactional
    public EquipmentIncidentDtos.Response record(Long reservationId, EquipmentIncidentDtos.CreateRequest request, String actor) {
        var reservation = reservations.findForUpdate(reservationId).orElseThrow(() -> new DomainException("RESERVATION_NOT_FOUND", "Không tìm thấy đặt phòng"));
        if (reservation.getStatus() != com.hospitality.mis.reservation.domain.ReservationStatus.CHECKED_IN)
            throw new DomainException("INVALID_STATE", "Chỉ ghi nhận sự cố khi khách đang ở");
        var room = reservation.getRooms().stream().map(line -> line.getRoom()).filter(candidate -> candidate.getId().equals(request.roomId())).findFirst()
                .orElseThrow(() -> new DomainException("ROOM_NOT_IN_RESERVATION", "Phòng không thuộc đặt phòng này"));
        var amount = pricing.equipmentCompensation(request.originalValue(), request.purchasedAt(), request.quantity(), LocalDate.now());
        var incident = incidents.save(new EquipmentIncident(reservation, room, request.equipmentName(), request.originalValue(), request.purchasedAt(), request.quantity(), amount));
        audit.record(actor, "EQUIPMENT_INCIDENT_RECORDED", "RESERVATION", reservationId.toString(), null, amount.toPlainString(), null);
        return new EquipmentIncidentDtos.Response(incident.getId(), request.roomId(), request.equipmentName(), amount);
    }
}
