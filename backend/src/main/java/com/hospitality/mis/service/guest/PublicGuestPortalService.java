package com.hospitality.mis.service.guest;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.billing.ServiceRepository;
import com.hospitality.mis.dao.room.ReservationOverlapPort;
import com.hospitality.mis.dao.room.RoomStore;
import com.hospitality.mis.dao.room.AmenityRepository;
import com.hospitality.mis.dao.room.RoomImageRepository;
import com.hospitality.mis.dto.guest.PublicGuestDtos;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomAvailabilityPolicy;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.entity.room.RoomType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Read model public cho khách; chỉ truy vấn dữ liệu đã được allow-list. */
@Service
public class PublicGuestPortalService {
    private final RoomStore rooms;
    private final ReservationOverlapPort overlaps;
    private final ServiceRepository services;
    private final RoomImageRepository images;
    private final AmenityRepository amenities;
    private final RoomAvailabilityPolicy availabilityPolicy = new RoomAvailabilityPolicy();

    public PublicGuestPortalService(RoomStore rooms, ReservationOverlapPort overlaps, ServiceRepository services,
                                    RoomImageRepository images, AmenityRepository amenities) {
        this.rooms = rooms;
        this.overlaps = overlaps;
        this.services = services;
        this.images = images;
        this.amenities = amenities;
    }

    /** Danh sách phòng công khai, lọc theo mã loại phòng nếu có. */
    @Transactional(readOnly = true)
    public List<PublicGuestDtos.RoomSummary> rooms(String type) {
        return rooms.search(type, null).stream().map(this::toSummary).toList();
    }

    /** Chi tiết phòng công khai hoặc lỗi ổn định nếu mã phòng không tồn tại. */
    @Transactional(readOnly = true)
    public PublicGuestDtos.RoomDetail room(String id) {
        Room room = rooms.findById(id)
                .orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        RoomType type = room.getRoomType();
        return new PublicGuestDtos.RoomDetail(
                room.getId(), room.getName(), type.getId(), type.getName(), type.getDescription(),
                type.getDailyPrice(), room.getFloor(), room.getDescription(), publicStatus(room.getStatus()),
                images.findByRoomIdAndActiveTrueOrderByDisplayOrderAscIdAsc(room.getId()).stream()
                        .map(image -> "/media/rooms/" + image.getRelativePath()).toList(),
                amenities.findActiveByRoomTypeId(type.getId()).stream().map(com.hospitality.mis.entity.room.Amenity::getName).toList());
    }

    /** Kiểm tra khả dụng theo khoảng khách chọn mà không lộ booking nào đang chiếm phòng. */
    @Transactional(readOnly = true)
    public List<PublicGuestDtos.RoomAvailability> availability(LocalDateTime from, LocalDateTime to, String type) {
        try {
            availabilityPolicy.validateInterval(from, to);
        } catch (IllegalArgumentException exception) {
            throw new DomainException("INVALID_INTERVAL", "Thời gian nhận phải trước thời gian trả");
        }
        return rooms.search(type, null).stream().map(room -> {
            RoomType roomType = room.getRoomType();
            boolean overlap = overlaps.hasOverlap(room.getId(), from, to);
            boolean available = availabilityPolicy.isAvailable(room, overlap);
            return new PublicGuestDtos.RoomAvailability(
                    room.getId(), room.getName(), roomType.getId(), roomType.getName(),
                    roomType.getDailyPrice(), room.getFloor(), publicStatus(room.getStatus()), available);
        }).toList();
    }

    /** Chỉ public các dịch vụ active; không để lộ tồn kho hoặc ngưỡng nội bộ. */
    @Transactional(readOnly = true)
    public List<PublicGuestDtos.ServiceSummary> services() {
        return services.findByActiveTrueOrderByNameAsc().stream()
                .map(service -> new PublicGuestDtos.ServiceSummary(
                        service.getId(), service.getName(), service.getPrice(), service.getUnit()))
                .toList();
    }

    private PublicGuestDtos.RoomSummary toSummary(Room room) {
        RoomType type = room.getRoomType();
        return new PublicGuestDtos.RoomSummary(room.getId(), room.getName(), type.getId(), type.getName(),
                type.getDailyPrice(), room.getFloor(), publicStatus(room.getStatus()));
    }

    /** Trạng thái nội bộ không thuộc public contract được ánh xạ fail-closed. */
    private PublicGuestDtos.PublicRoomStatus publicStatus(RoomStatus status) {
        if (status == null) return PublicGuestDtos.PublicRoomStatus.OUT_OF_SERVICE;
        return switch (status) {
            case READY -> PublicGuestDtos.PublicRoomStatus.READY;
            case RESERVED -> PublicGuestDtos.PublicRoomStatus.RESERVED;
            case OCCUPIED -> PublicGuestDtos.PublicRoomStatus.OCCUPIED;
            case CLEANING -> PublicGuestDtos.PublicRoomStatus.CLEANING;
            case MAINTENANCE -> PublicGuestDtos.PublicRoomStatus.MAINTENANCE;
            case OUT_OF_SERVICE -> PublicGuestDtos.PublicRoomStatus.OUT_OF_SERVICE;
            case RETURNED, CANCELLED -> PublicGuestDtos.PublicRoomStatus.OUT_OF_SERVICE;
        };
    }
}
