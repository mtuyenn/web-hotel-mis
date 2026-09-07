package com.hospitality.mis.room;

import com.hospitality.mis.room.adapter.RoomRepository;
import com.hospitality.mis.room.adapter.RoomTypeRepository;
import com.hospitality.mis.room.domain.Room;
import com.hospitality.mis.room.domain.RoomStatus;
import com.hospitality.mis.room.domain.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:roompersistence;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoomPersistenceTest {
    @Autowired RoomRepository rooms;
    @Autowired RoomTypeRepository roomTypes;

    @Test
    void canonicalRoomAndTypePersistAndReload() {
        RoomType type = new RoomType();
        type.setId("STD"); type.setName("Standard"); type.setDailyPrice(new BigDecimal("2400.00"));
        roomTypes.saveAndFlush(type);
        Room room = new Room();
        room.setId("R101"); room.setName("Room 101"); room.setFloor(1);
        room.setRoomType(type); room.setStatus(RoomStatus.READY);
        rooms.saveAndFlush(room);

        Room reloaded = rooms.findForUpdate("R101").orElseThrow();
        assertThat(reloaded.getRoomType().getId()).isEqualTo("STD");
        assertThat(reloaded.getStatus()).isEqualTo(RoomStatus.READY);
        assertThat(reloaded.getVersion()).isZero();
    }
}
