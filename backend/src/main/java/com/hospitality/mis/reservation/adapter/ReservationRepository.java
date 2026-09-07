package com.hospitality.mis.reservation.adapter;

import com.hospitality.mis.reservation.domain.Reservation;
import com.hospitality.mis.reservation.domain.ReservationStatus;
import com.hospitality.mis.room.domain.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"guest", "employee", "rooms", "rooms.room", "rooms.room.roomType", "serviceUsages", "serviceUsages.service"})
    @Query("select distinct r from Reservation r where r.id = :id")
    Optional<Reservation> findDetails(@Param("id") Long id);

    Optional<Reservation> findByIdempotencyKey(String idempotencyKey);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.idempotencyKey = :key")
    Optional<Reservation> findByIdempotencyKeyForUpdate(@Param("key") String key);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct r from Reservation r join fetch r.guest join fetch r.rooms rr join fetch rr.room join fetch rr.room.roomType where r.id = :id")
    Optional<Reservation> findForUpdate(@Param("id") Long id);

    @Query("select count(rr) > 0 from ReservationRoom rr where rr.room.id = :roomId and rr.checkIn < :to and rr.checkOut > :from and rr.status <> :cancelledRoom and rr.reservation.status not in :ignoredReservations")
    boolean hasOverlap(@Param("roomId") String roomId, @Param("from") LocalDateTime from,
                       @Param("to") LocalDateTime to, @Param("cancelledRoom") RoomStatus cancelledRoom,
                       @Param("ignoredReservations") List<ReservationStatus> ignoredReservations);

    @Query("select count(rr) > 0 from ReservationRoom rr where rr.room.id = :roomId and rr.checkIn < :to and rr.checkOut > :from and rr.reservation.id <> :excludedReservationId and rr.status <> :cancelledRoom and rr.reservation.status not in :ignoredReservations")
    boolean hasOverlapExcludingReservation(@Param("excludedReservationId") Long excludedReservationId,
                                           @Param("roomId") String roomId,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to,
                                           @Param("cancelledRoom") RoomStatus cancelledRoom,
                                           @Param("ignoredReservations") List<ReservationStatus> ignoredReservations);
}
