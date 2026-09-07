package com.hospitality.mis.operations.application;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.application.AuditService;
import com.hospitality.mis.operations.adapter.RoomTransferRepository;
import com.hospitality.mis.operations.api.RoomTransferDtos;
import com.hospitality.mis.reservation.domain.RoomTransfer;
import com.hospitality.mis.reservation.adapter.ReservationRepository;
import com.hospitality.mis.reservation.domain.ReservationStatus;
import com.hospitality.mis.room.adapter.RoomRepository;
import com.hospitality.mis.room.domain.RoomStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class RoomTransferService {
    private final ReservationRepository reservations; private final RoomRepository rooms;
    private final RoomTransferRepository transfers; private final AuditService audit;
    public RoomTransferService(ReservationRepository reservations, RoomRepository rooms, RoomTransferRepository transfers, AuditService audit) {
        this.reservations = reservations; this.rooms = rooms; this.transfers = transfers; this.audit = audit;
    }
    @Transactional
    public RoomTransferDtos.Response transfer(Long reservationId, RoomTransferDtos.CreateRequest request, String actor) {
        if (request.fromRoomId().equals(request.toRoomId())) throw new DomainException("SAME_ROOM", "Phòng chuyển đến phải khác phòng hiện tại");
        var reservation = reservations.findForUpdate(reservationId).orElseThrow(() -> new DomainException("RESERVATION_NOT_FOUND", "Không tìm thấy đặt phòng"));
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) throw new DomainException("INVALID_STATE", "Chỉ chuyển phòng khi khách đang ở");
        var detail = reservation.getRooms().stream().filter(d -> d.getRoom().getId().equals(request.fromRoomId())).findFirst()
                .orElseThrow(() -> new DomainException("ROOM_NOT_IN_RESERVATION", "Phòng hiện tại không thuộc đặt phòng"));
        var locked = rooms.findAllForUpdateOrdered(java.util.List.of(request.fromRoomId(), request.toRoomId()));
        var to = locked.stream().filter(r -> r.getId().equals(request.toRoomId())).findFirst().orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng đích"));
        if (to.getStatus() != RoomStatus.READY) throw new DomainException("ROOM_NOT_AVAILABLE", "Phòng đích không sẵn sàng");
        var from = detail.getRoom(); detail.setRoom(to); detail.incrementTransferCount();
        from.setStatus(RoomStatus.CLEANING); to.setStatus(RoomStatus.OCCUPIED);
        var transfer = new RoomTransfer();
        transfer.setReservation(reservation); transfer.setFromRoom(from); transfer.setToRoom(to);
        transfer.setTransferredAt(request.transferredAt() == null ? LocalDateTime.now() : request.transferredAt());
        transfer.setReason(request.reason());
        var saved = transfers.save(transfer);
        audit.record(actor, "ROOM_TRANSFERRED", "RESERVATION", reservationId.toString(), request.fromRoomId(), request.toRoomId(), request.reason());
        return new RoomTransferDtos.Response(saved.getId(), reservationId, from.getId(), to.getId(), request.transferredAt(), request.reason());
    }
}
