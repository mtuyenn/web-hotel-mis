package com.hospitality.mis.guest;

import com.hospitality.mis.guest.adapter.GuestRepository;
import com.hospitality.mis.guest.domain.MembershipTier;
import com.hospitality.mis.guest.domain.Guest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:guestpersistence;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GuestPersistenceTest {
    @Autowired GuestRepository guests;

    @BeforeEach
    void cleanGuests() {
        guests.deleteAllInBatch();
    }

    @Test
    void canonicalFieldsPersistToTheGuestEntity() {
        Guest guest = new Guest();
        guest.setFullName("Tran Thi Binh");
        guest.setAddress("Quan 3");
        guest.setPhone("0912345678");
        guest.setEmail("binh@example.test");
        guest.setIdentityNumber("098765432109");
        guest.setBirthYear(1988);
        guest.setMembershipTier(MembershipTier.VIP);
        guest.setTotalSpend(new BigDecimal("12000000.00"));
        guest.setLateCancellationCount(2);
        guest.setBookingBlocked(true);

        Long id = guests.saveAndFlush(guest).getId();
        Guest reloaded = guests.findById(id).orElseThrow();

        assertThat(reloaded.getFullName()).isEqualTo("Tran Thi Binh");
        assertThat(reloaded.getAddress()).isEqualTo("Quan 3");
        assertThat(reloaded.getPhone()).isEqualTo("0912345678");
        assertThat(reloaded.getIdentityNumber()).isEqualTo("098765432109");
        assertThat(reloaded.getBirthYear()).isEqualTo(1988);
        assertThat(reloaded.getMembershipTier()).isEqualTo(MembershipTier.VIP);
        assertThat(reloaded.getTotalSpend()).isEqualByComparingTo("12000000.00");
        assertThat(reloaded.getLateCancellationCount()).isEqualTo(2);
        assertThat(reloaded.isBookingBlocked()).isTrue();
    }

    @Test
    void canonicalSearchMatchesNameIdentityAndPhone() {
        Guest guest = new Guest();
        guest.setFullName("Le Hoang Cuong");
        guest.setPhone("0923456789");
        guest.setEmail("cuong@example.test");
        guest.setIdentityNumber("112233445566");
        guests.saveAndFlush(guest);

        assertThat(guests.search("hoang")).extracting(Guest::getId).hasSize(1);
        assertThat(guests.search("112233")).hasSize(1);
        assertThat(guests.search("092345")).hasSize(1);
    }
}
