package com.hospitality.mis.operations.adapter;
import com.hospitality.mis.reservation.domain.RoomTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RoomTransferRepository extends JpaRepository<RoomTransfer, Long> {
    List<RoomTransfer> findByReservationIdOrderByTransferredAtDesc(Long reservationId);
}
