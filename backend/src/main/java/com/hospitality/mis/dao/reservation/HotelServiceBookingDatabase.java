package com.hospitality.mis.dao.reservation;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dto.reservation.HotelServiceBookingDtos;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Persistence contract for hotel-operated services: views for reads, procedures for commands. */
@Repository
public class HotelServiceBookingDatabase {
    private static final String READ="SELECT maDatDichVuKhachSan,maPhieuDatPhong,maPhong,maDichVu,tenDichVu,thoiDiemDuKien,soLuong,soLuongMienPhi,donGia,buoiAn,trangThai,ghiChu,thoiDiemKetThuc,hanThanhToan,soTienDaThanhToan,maThamChieuThanhToan,maPhanHoiThanhToan FROM dbo.vwDatDichVuKhachSan";
    private final JdbcTemplate jdbc;

    public HotelServiceBookingDatabase(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<HotelServiceBookingDtos.Response> list(long reservationId, Long customerId) {
        Integer owned = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.vwChuSoHuuDatPhong WHERE maPhieuDatPhong=? AND (? IS NULL OR maTaiKhoanKhachHang=?)",
                Integer.class, reservationId, customerId, customerId);
        if (owned == null || owned == 0) throw new DomainException("RESERVATION_NOT_FOUND", "Không tìm thấy đặt phòng");
        return reservationServices(reservationId);
    }

    public List<HotelServiceBookingDtos.Response> reservationServices(long reservationId) {
        return jdbc.query(READ+" WHERE maPhieuDatPhong=? ORDER BY thoiDiemDuKien,maDatDichVuKhachSan",
                (rs, n) -> fromResultSet(rs), reservationId);
    }

    public List<HotelServiceBookingDtos.Response> coordinatedBookings(LocalDate date, String status) {
        return jdbc.query(READ+"""
                 WHERE CAST(thoiDiemDuKien AS DATE)=?
                AND (? IS NULL OR trangThai=?) ORDER BY thoiDiemDuKien,maPhong,maDatDichVuKhachSan
                """, (rs, n) -> fromResultSet(rs), date, status, status);
    }

    public HotelServiceBookingDtos.Response find(long id) {
        var rows = jdbc.query(READ+" WHERE maDatDichVuKhachSan=?",
                (rs, n) -> fromResultSet(rs), id);
        if (rows.isEmpty()) throw new DomainException("SERVICE_BOOKING_NOT_FOUND", "Không tìm thấy dịch vụ");
        return rows.get(0);
    }

    public HotelServiceBookingDtos.Response book(HotelServiceBookingDtos.Request request, String meal, String key,
            String fingerprint, String actor, Long customerId, boolean atDesk, LocalDateTime now) {
        try {
            Long id = jdbc.queryForObject("EXEC dbo.uspDatDichVuKhachSan ?,?,?,?,?,?,?,?,?,?,?,?,?", Long.class,
                    request.reservationId(), request.roomId(), request.serviceId(), request.scheduledAt(), request.quantity(),
                    meal, request.note(), key, fingerprint, actor, customerId, atDesk, now);
            return find(id);
        } catch (DataAccessException error) { throw translate(error); }
    }

    public HotelServiceBookingDtos.Response use(long reservationId, long id, String actor, LocalDateTime now) {
        try {
            jdbc.update("EXEC dbo.uspXacNhanSuDungDichVu ?,?,?,?", id, actor, now, reservationId);
            return find(id);
        } catch (DataAccessException error) { throw translate(error); }
    }

    public HotelServiceBookingDtos.Response cancel(long id, String actor, long customerId) {
        try {
            jdbc.update("EXEC dbo.uspHuyDatDichVu ?,?,?", id, actor, customerId);
            return find(id);
        } catch (DataAccessException error) { throw translate(error); }
    }

    public void cancelReservation(long reservationId, String actor) {
        jdbc.update("EXEC dbo.uspHuyDichVuTheoDatPhong ?,?", reservationId, actor);
    }

    public BigDecimal usedTotal(long reservationId) {
        return jdbc.queryForObject("SELECT dbo.fnTongDichVuKhachSanDaDung(?)", BigDecimal.class, reservationId);
    }

    public void preparePayment(long id,long customer,LocalDateTime now){try{jdbc.update("EXEC dbo.uspThanhToanDichVu ?,?,NULL,NULL,NULL,NULL,0,?",id,customer,now);}catch(DataAccessException e){throw translate(e);}}
    public HotelServiceBookingDtos.Response paymentBooking(long id,long customer){var b=find(id);list(b.reservationId(),customer);return b;}
    public HotelServiceBookingDtos.Response completePayment(long id,String merchant,BigDecimal amount,String transaction,String response,boolean paid,LocalDateTime now){
        try{jdbc.update("EXEC dbo.uspThanhToanDichVu ?,NULL,?,?,?,?,?,?",id,merchant,amount,transaction,response,paid,now);return find(id);}catch(DataAccessException e){throw translate(e);}
    }
    private RuntimeException translate(DataAccessException error) {
        for (Throwable cause=error; cause!=null; cause=cause.getCause()) {
            if (!(cause instanceof SQLException sql)) continue;
            String code = switch (sql.getErrorCode()) {
                case 51001 -> "RESERVATION_NOT_FOUND";
                case 51002, 53611 -> "INVALID_STATE";
                case 51005 -> "IDEMPOTENCY_KEY_CONFLICT";
                case 51006 -> "INSUFFICIENT_STOCK";
                case 51101 -> "SERVICE_BOOKING_NOT_FOUND";
                case 51102 -> "SERVICE_NOT_DUE";
                case 51103 -> "INVALID_SERVICE_BOOKING";
                case 51104 -> "MEAL_PERIOD_REQUIRED";
                case 51105 -> "INVALID_MEAL_PERIOD";
                case 51106 -> "POOL_WALK_IN_ONLY";
                case 51107 -> "DEPOSIT_NOT_PAID";
                case 51108, 53610 -> "SERVICE_OUTSIDE_STAY";
                case 51109 -> "SERVICE_NOT_FOUND";
                case 51110 -> "SERVICE_PRICE_NOT_SET";
                case 51112 -> "SERVICE_PAYMENT_EXPIRED";case 51113 -> "SERVICE_PAYMENT_MISMATCH";
                case 53609 -> "SERVICE_OUTSIDE_MEAL_HOURS";
                default -> null;
            };
            if (code != null) return new DomainException(code, sql.getMessage());
        }
        return error;
    }
    private static HotelServiceBookingDtos.Response fromResultSet(ResultSet rs) throws SQLException {
        BigDecimal price = rs.getBigDecimal("donGia");
        int quantity = rs.getInt("soLuong"), free = rs.getInt("soLuongMienPhi");
        String name = rs.getString("tenDichVu");
        if (name == null || name.isBlank()) name = rs.getString("maDichVu");
        return new HotelServiceBookingDtos.Response(rs.getLong("maDatDichVuKhachSan"), rs.getLong("maPhieuDatPhong"), rs.getString("maPhong"),
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

    private static String bookingStatusCode(String value) {
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

    private static String mealPeriodCode(String value) {
        if (value == null) return null;
        return switch (value) {
            case "Bữa trưa" -> "LUNCH";
            case "Bữa tối" -> "DINNER";
            default -> throw new IllegalArgumentException("Buổi ăn không hợp lệ: " + value);
        };
    }
}
