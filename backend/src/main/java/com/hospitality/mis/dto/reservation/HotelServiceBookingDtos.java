package com.hospitality.mis.dto.reservation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** API and read-model types for hotel service bookings. Kept outside service/DAO layers. */
public final class HotelServiceBookingDtos {
    private HotelServiceBookingDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Request(Long reservationId, String roomId, String serviceId, LocalDateTime scheduledAt,
                          Integer quantity, String mealPeriod, String note) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long reservationId, String roomId, String serviceId, String serviceName,
                           LocalDateTime scheduledAt, int quantity, int freeQuantity, BigDecimal unitPrice,
                           BigDecimal amountDue, String mealPeriod, String status, String note, LocalDateTime endsAt,
                           LocalDateTime paymentExpiresAt, BigDecimal paidAmount, String paymentReference,
                           String paymentResponse) {
        public Response(Long id, Long reservationId, String roomId, String serviceId, LocalDateTime scheduledAt,
                        int quantity, int freeQuantity, BigDecimal unitPrice, BigDecimal amountDue,
                        String mealPeriod, String status, String note) {
            this(id, reservationId, roomId, serviceId, serviceId, scheduledAt, quantity, freeQuantity, unitPrice,
                    amountDue, mealPeriod, status, note, scheduledAt.plusHours(1), null, BigDecimal.ZERO, null, null);
        }
    }

    public static Response fromResultSet(ResultSet rs) throws SQLException {
        BigDecimal price = rs.getBigDecimal("donGia");
        int quantity = rs.getInt("soLuong"), free = rs.getInt("soLuongMienPhi");
        String name = rs.getString("tenDichVu");
        if (name == null || name.isBlank()) name = rs.getString("maDichVu");
        return new Response(rs.getLong("maDatDichVuKhachSan"), rs.getLong("maPhieuDatPhong"), rs.getString("maPhong"),
                rs.getString("maDichVu"), name, rs.getTimestamp("thoiDiemDuKien").toLocalDateTime(), quantity, free,
                price, price.multiply(BigDecimal.valueOf(quantity - free)), mealPeriodCode(rs.getString("buoiAn")),
                bookingStatusCode(rs.getString("trangThai")), rs.getString("ghiChu"), date(rs, "thoiDiemKetThuc"),
                date(rs, "hanThanhToan"), rs.getBigDecimal("soTienDaThanhToan"), rs.getString("maThamChieuThanhToan"),
                rs.getString("maPhanHoiThanhToan"));
    }

    private static LocalDateTime date(ResultSet rs, String name) throws SQLException {
        var value = rs.getTimestamp(name);
        return value == null ? null : value.toLocalDateTime();
    }

    public static String bookingStatusCode(String value) {
        if (value == null) return null;
        return switch (value) {
            case "Chờ thanh toán" -> "PENDING_PAYMENT";
            case "Hết hạn" -> "EXPIRED";
            case "Đã xác nhận" -> "CONFIRMED";
            case "Đã sử dụng" -> "USED";
            case "Đã hủy" -> "CANCELLED";
            default -> throw new IllegalArgumentException("Trạng thái đặt dịch vụ không hợp lệ: " + value);
        };
    }

    public static String mealPeriodCode(String value) {
        if (value == null) return null;
        return switch (value) {
            case "Bữa trưa" -> "LUNCH";
            case "Bữa tối" -> "DINNER";
            default -> throw new IllegalArgumentException("Buổi ăn không hợp lệ: " + value);
        };
    }
}
