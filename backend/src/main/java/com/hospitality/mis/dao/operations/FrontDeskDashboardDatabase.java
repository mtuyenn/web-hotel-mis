package com.hospitality.mis.dao.operations;

import com.hospitality.mis.dto.operations.FrontDeskDashboardDtos;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.dao.room.RoomStatusConverter;
import com.hospitality.mis.persistence.VietnameseEnumConverters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FrontDeskDashboardDatabase {
    private final JdbcTemplate jdbc;
    private final VietnameseEnumConverters.ReservationStatusConverter statuses=new VietnameseEnumConverters.ReservationStatusConverter();
    private final VietnameseEnumConverters.DepositPaymentStatusConverter deposits=new VietnameseEnumConverters.DepositPaymentStatusConverter();
    public FrontDeskDashboardDatabase(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public long count(String query,ReservationStatus status,LocalDateTime from,LocalDateTime to){
        return jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo.fnDashboardDatPhong(?,?,'ALL',?,?)",Long.class,query,statuses.convertToDatabaseColumn(status),from,to);
    }
    public List<FrontDeskDashboardDtos.ReservationItem> items(String query,ReservationStatus status,String bucket,LocalDateTime from,LocalDateTime to,int page,int size){
        var items=jdbc.query("SELECT maPhieuDatPhong,maKhachLuuTru,hoVaTen,soDienThoai,trangThai,thoiDiemNhanPhong,thoiDiemTraPhong,tienDatCoc,trangThaiThanhToanCoc,soTienPhaiTra FROM dbo.fnDashboardDatPhong(?,?,?,?,?) ORDER BY thoiDiemNhanPhong,maPhieuDatPhong OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",this::reservation,query,statuses.convertToDatabaseColumn(status),bucket,from,to,(long)page*size,size);
        if(items.isEmpty())return items;
        // Load room assignments for this bounded page together. A query from the
        // row mapper costs one Azure round trip for every reservation otherwise.
        Map<Long,List<String>> roomIds=new HashMap<>();
        String placeholders=String.join(",",Collections.nCopies(items.size(),"?"));
        jdbc.query("SELECT maPhieuDatPhong,maPhong FROM dbo.vwDatPhongChiTiet WHERE maPhieuDatPhong IN ("+placeholders+") AND trangThaiPhongTrongDatPhong IN(N'Giữ tạm phòng',N'Đã giữ phòng',N'Đang có khách') ORDER BY maPhieuDatPhong,maPhong",
                (org.springframework.jdbc.core.RowCallbackHandler)r->roomIds.computeIfAbsent(r.getLong(1),ignored->new ArrayList<>()).add(r.getString(2)),
                items.stream().map(FrontDeskDashboardDtos.ReservationItem::reservationId).toArray());
        return items.stream().map(item->new FrontDeskDashboardDtos.ReservationItem(item.reservationId(),item.guestId(),item.guestName(),item.guestPhone(),item.status(),item.checkIn(),item.checkOut(),List.copyOf(roomIds.getOrDefault(item.reservationId(),List.of())),item.depositAmount(),item.depositPaymentStatus(),item.invoiceBalance())).toList();
    }
    public List<FrontDeskDashboardDtos.RoomSummary> rooms(){
        return jdbc.query("SELECT maPhong,ten,trangThai,maLoaiPhong,tenLoaiPhong,tang,giaTheoNgay,loaiGiuong FROM dbo.vwPhongNoiBo ORDER BY maPhong",(r,n)->new FrontDeskDashboardDtos.RoomSummary(r.getString(1),r.getString(2),new RoomStatusConverter().convertToEntityAttribute(r.getString(3)),r.getString(4),r.getString(5),(Integer)r.getObject(6),r.getBigDecimal(7),r.getString(8)));
    }
    public List<FrontDeskDashboardDtos.IncidentItem> incidents(int page,int size){
        return jdbc.query("SELECT maSuCoThietBi,maPhieuDatPhong,maPhong,tienBoiThuong FROM dbo.vwSuCoLeTan ORDER BY maSuCoThietBi OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",(r,n)->new FrontDeskDashboardDtos.IncidentItem(r.getLong(1),(Long)r.getObject(2),r.getString(3),r.getBigDecimal(4)),(long)page*size,size);
    }
    private FrontDeskDashboardDtos.ReservationItem reservation(ResultSet r,int n)throws SQLException{
        long id=r.getLong(1);
        var deposit=deposits.convertToEntityAttribute(r.getString(9));
        return new FrontDeskDashboardDtos.ReservationItem(id,(Long)r.getObject(2),r.getString(3),r.getString(4),statuses.convertToEntityAttribute(r.getString(5)),dateTime(r,6),dateTime(r,7),List.of(),r.getBigDecimal(8),deposit==null?null:deposit.name(),r.getBigDecimal(10));
    }
    private static LocalDateTime dateTime(ResultSet r,int index)throws SQLException{var value=r.getTimestamp(index);return value==null?null:value.toLocalDateTime();}
}
