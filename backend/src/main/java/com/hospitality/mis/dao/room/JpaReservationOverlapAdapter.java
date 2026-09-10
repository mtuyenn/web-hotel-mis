package com.hospitality.mis.dao.room;



import com.hospitality.mis.dao.room.ReservationOverlapPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;



import java.time.LocalDateTime;



/**

 * Adapts the existing reservation overlap query to the room read port.

 * No reservation entity, table, endpoint or behavior is introduced here.

 */

@Component

public class JpaReservationOverlapAdapter implements ReservationOverlapPort {
    @PersistenceContext
    private EntityManager entityManager;

    public JpaReservationOverlapAdapter() {}


    @Override

    public boolean hasOverlap(String roomId, LocalDateTime from, LocalDateTime to) {

        Number count = (Number) entityManager.createNativeQuery("""
                select count(*)
                from reservation_rooms rr
                join reservations r on r.id = rr.reservation_id
                where rr.room_id = :roomId
                  and rr.check_in < :to and rr.check_out > :from
                  and rr.status <> 'DA_HUY'
                  and r.status not in ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT')
                """)
                .setParameter("roomId", roomId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return count.longValue() > 0;
    }
}
