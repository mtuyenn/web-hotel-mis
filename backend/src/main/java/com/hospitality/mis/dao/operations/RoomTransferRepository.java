package com.hospitality.mis.dao.operations;
import com.hospitality.mis.entity.reservation.RoomTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RoomTransferRepository extends JpaRepository<RoomTransfer, Long> {
    List<RoomTransfer> findByReservationIdOrderByTransferredAtDesc(Long reservationId);
}
