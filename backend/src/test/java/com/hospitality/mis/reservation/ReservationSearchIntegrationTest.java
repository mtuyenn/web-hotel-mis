package com.hospitality.mis.reservation;

import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.entity.guest.Guest;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.identity.EmployeeRole;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class ReservationSearchIntegrationTest {
    @Autowired EntityManager em;
    @Autowired ReservationRepository repository;

    @Test
    void scopeAndStatusAreAppliedBeforePaginationIncludingOlderBookings() {
        Employee owner = employee("owner", "0900000001");
        Employee other = employee("other", "0900000002");
        Guest guest = new Guest(); guest.setFullName("Search Guest"); guest.setPhone("0900000011");
        guest.setIdentityNumber("012345678901"); em.persist(guest);
        Reservation old = reservation(owner, guest, ReservationStatus.CONFIRMED, 0);
        for (int i = 1; i <= 105; i++) reservation(other, guest, ReservationStatus.CONFIRMED, i);
        reservation(owner, guest, ReservationStatus.DEPOSIT_PAID, 106);
        em.flush(); em.clear();
        var page = repository.searchIds("owner", ReservationStatus.CONFIRMED, guest.getId(), PageRequest.of(0, 20));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).containsExactly(old.getId());
        assertThat(repository.findPageDetails(page.getContent())).hasSize(1);
        var global = repository.searchIds(null, null, null, PageRequest.of(1, 20));
        assertThat(global.getTotalElements()).isEqualTo(107);
        assertThat(global.getContent()).hasSize(20);
        assertThat(repository.searchIds("owner", null, null, PageRequest.of(1, 1)).getContent())
                .containsExactly(old.getId());
    }

    private Employee employee(String id, String phone) {
        Employee e = new Employee(); e.setEmployeeId(id); e.setFullName(id); e.setPhone(phone);
        e.setPassword("test-hash"); e.setRole(EmployeeRole.FRONT_DESK); em.persist(e); return e;
    }
    private Reservation reservation(Employee employee, Guest guest, ReservationStatus status, int minute) {
        Reservation r = new Reservation(); r.setEmployee(employee); r.setGuest(guest); r.transitionTo(status);
        ReflectionTestUtils.setField(r, "bookedAt", LocalDateTime.of(2031, 1, 1, 0, 0).plusMinutes(minute));
        em.persist(r); return r;
    }
}
