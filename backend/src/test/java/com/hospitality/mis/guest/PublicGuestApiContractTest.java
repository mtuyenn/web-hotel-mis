package com.hospitality.mis.guest;

import com.hospitality.mis.dao.billing.ServiceRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dao.room.RoomTypeRepository;
import com.hospitality.mis.entity.billing.Service;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.hospitality.mis.service.guest.PublicGuestPortalService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=${MIGRATION_TEST_DB_URL}",
        "spring.datasource.username=${MIGRATION_TEST_DB_USERNAME}",
        "spring.datasource.password=${MIGRATION_TEST_DB_PASSWORD}",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
/** Kiểm tra public portal anonymous, DTO allow-list và lọc dịch vụ active. */
class PublicGuestApiContractTest {
    @Autowired MockMvc mockMvc;
    @Autowired RoomRepository rooms;
    @Autowired RoomTypeRepository roomTypes;
    @Autowired ServiceRepository services;
    @Autowired JdbcTemplate jdbc;
    @Autowired PublicGuestPortalService portal;

    @BeforeEach
    void seedPublicCatalog() {
        RoomType type = new RoomType();
        type.setId("STD");
        type.setName("Standard");
        type.setDailyPrice(new BigDecimal("2400000.00"));
        type.setDescription("Phòng tiêu chuẩn");
        roomTypes.saveAndFlush(type);

        Room room = new Room();
        room.setId("R101");
        room.setName("Phòng 101");
        room.setFloor(1);
        room.setDescription("Phòng có cửa sổ");
        room.setRoomType(type);
        rooms.saveAndFlush(room);

        Service active = new Service();
        active.setId("WATER");
        active.setName("Nước suối");
        active.setPrice(new BigDecimal("15000.00"));
        active.setUnit("CHAI");
        active.setStockQuantity(20);
        active.setSafetyThreshold(5);
        active.setActive(true);
        services.saveAndFlush(active);

        Service inactive = new Service();
        inactive.setId("OLD");
        inactive.setName("Dịch vụ ngừng phục vụ");
        inactive.setPrice(new BigDecimal("50000.00"));
        inactive.setUnit("LẦN");
        inactive.setStockQuantity(99);
        inactive.setSafetyThreshold(0);
        inactive.setActive(false);
        services.saveAndFlush(inactive);
    }

    @Test
    void anonymousCanReadPublicRoomsAndInternalRoomApiRemainsProtected() throws Exception {
        mockMvc.perform(get("/api/public/rooms"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=30")))
                .andExpect(jsonPath("$[0].room_id").value("R101"))
                .andExpect(jsonPath("$[0].room_name").value("Phòng 101"))
                .andExpect(jsonPath("$[0].room_type_name").value("Standard"))
                .andExpect(jsonPath("$[0].daily_price").value(2400000.00))
                .andExpect(jsonPath("$[0].status").value("available"))
                .andExpect(jsonPath("$[0].guest").doesNotExist())
                .andExpect(jsonPath("$[0].stock").doesNotExist());

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicRoomDetailUsesAnAllowListedDto() throws Exception {
        mockMvc.perform(get("/api/public/rooms/R101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.room_id").value("R101"))
                .andExpect(jsonPath("$.room_type_description").value("Phòng tiêu chuẩn"))
                .andExpect(jsonPath("$.description").value("Phòng có cửa sổ"))
                .andExpect(jsonPath("$.image_urls").isEmpty())
                .andExpect(jsonPath("$.amenities").isEmpty())
                .andExpect(jsonPath("$.reservation").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist());
    }

    @Test
    void publicServicesOnlyExposeActiveCatalogWithoutInventoryFields() throws Exception {
        mockMvc.perform(get("/api/public/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service_id").value("WATER"))
                .andExpect(jsonPath("$[0].name").value("Nước suối"))
                .andExpect(jsonPath("$[0].price").value(15000.00))
                .andExpect(jsonPath("$[0].stock").doesNotExist())
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void anonymousCanCheckAvailabilityForASelectedInterval() throws Exception {
        mockMvc.perform(get("/api/public/rooms/availability")
                        .param("from", "2031-01-10T14:00:00")
                        .param("to", "2031-01-11T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(jsonPath("$[0].room_id").value("R101"))
                .andExpect(jsonPath("$[0].current_status").value("available"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void roomListsAndAvailabilityPreservePublicMediaFallbacksAcrossTheCatalog() {
        for (int number=102;number<=113;number++) {
            jdbc.update("INSERT dbo.Phong(maPhong,maLoaiPhong) VALUES(?,N'STD')","R"+number);
        }
        jdbc.update("INSERT dbo.LoaiPhong(maLoaiPhong,ten,giaTheoNgay,duongDanAnhBia) VALUES(N'COVER',N'Cover',100000,N'https://example.test/cover.jpg'),(N'EMPTY',N'Empty',100000,NULL)");
        jdbc.update("INSERT dbo.Phong(maPhong,maLoaiPhong) VALUES(N'R201',N'COVER'),(N'R202',N'EMPTY')");
        jdbc.update("INSERT dbo.HinhAnhLoaiPhong(maLoaiPhong,duongDanAnh,thuTuHienThi) VALUES(N'STD',N'https://example.test/type.jpg',0)");
        jdbc.update("INSERT dbo.HinhAnhPhong(maPhong,duongDanTuongDoi,thuTuHienThi,loaiNoiDung,kichThuocByte,dangHoatDong) VALUES(N'R101',N'inactive.jpg',0,N'image/jpeg',100,0),(N'R101',N'room.jpg',1,N'image/jpeg',100,1),(N'R101',N'https://example.test/room-second.jpg',2,N'image/jpeg',100,1)");
        jdbc.update("INSERT dbo.TienNghi(ten,dangHoatDong) VALUES(N'Alpha test',1),(N'Zulu test',1),(N'Hidden test',0)");
        jdbc.update("INSERT dbo.LoaiPhongTienNghi(maLoaiPhong,maTienNghi) SELECT N'STD',maTienNghi FROM dbo.TienNghi WHERE ten IN(N'Alpha test',N'Zulu test',N'Hidden test')");

        var summaries=portal.rooms(null);
        assertThat(summaries).hasSize(15);
        var byId=summaries.stream().collect(Collectors.toMap(row->row.roomId(),Function.identity()));
        assertThat(byId.get("R101").imageUrl()).isEqualTo("/media/rooms/room.jpg");
        assertThat(byId.get("R102").imageUrl()).isEqualTo("https://example.test/type.jpg");
        assertThat(byId.get("R201").imageUrl()).isEqualTo("https://example.test/cover.jpg");
        assertThat(byId.get("R202").imageUrl()).isNull();
        assertThat(byId.get("R101").amenities()).containsExactly("Alpha test","Zulu test");
        for (var summary:summaries) {
            var detail=portal.room(summary.roomId());
            assertThat(summary.imageUrl()).isEqualTo(detail.imageUrls().stream().findFirst().orElse(null));
            assertThat(summary.amenities()).isEqualTo(detail.amenities());
        }
        var availability=portal.availability(LocalDateTime.of(2031,1,10,14,0),LocalDateTime.of(2031,1,11,12,0),null);
        assertThat(availability).hasSize(15);
        for (var room:availability) {
            assertThat(room.available()).isTrue();
            assertThat(room.imageUrl()).isEqualTo(byId.get(room.roomId()).imageUrl());
            assertThat(room.amenities()).isEqualTo(byId.get(room.roomId()).amenities());
        }
        assertThat(portal.rooms("STD")).hasSize(13);
        assertThat(portal.availability(LocalDateTime.of(2031,1,10,14,0),LocalDateTime.of(2031,1,11,12,0),"COVER"))
                .extracting(row->row.roomId()).containsExactly("R201");
    }

    @Test
    void publicCatalogValidatesPagination() throws Exception {
        mockMvc.perform(get("/api/public/rooms").param("page", "-1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    @Test
    void publicAvailabilityRejectsAnInvalidRange() throws Exception {
        mockMvc.perform(get("/api/public/rooms/availability")
                        .param("from", "2031-01-11T12:00:00")
                        .param("to", "2031-01-10T14:00:00"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_INTERVAL"));
    }
}
