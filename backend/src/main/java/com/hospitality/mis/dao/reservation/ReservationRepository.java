package com.hospitality.mis.dao.reservation;
import com.hospitality.mis.entity.reservation.ReservationRoom;




import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.entity.room.RoomStatus;
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
    // Page IDs first: paginating a collection fetch would load the whole result in memory.
    @Query("select r.id from Reservation r where (:owner is null or r.employee.employeeId = :owner) "
            + "and (:status is null or r.status = :status) and (:guestId is null or r.guest.id = :guestId) "
            + "order by r.bookedAt desc, r.id desc")
    org.springframework.data.domain.Page<Long> searchIds(@Param("owner") String owner,
            @Param("status") ReservationStatus status, @Param("guestId") Long guestId,
            org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"guest", "employee", "rooms", "rooms.room"})
    @Query("select distinct r from Reservation r where r.id in :ids")
    List<Reservation> findPageDetails(@Param("ids") List<Long> ids);
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
