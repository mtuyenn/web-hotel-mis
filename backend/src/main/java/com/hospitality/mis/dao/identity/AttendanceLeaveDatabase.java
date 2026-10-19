package com.hospitality.mis.dao.identity;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dto.identity.AttendanceLeaveDtos;
import com.hospitality.mis.persistence.VietnameseCodeConverters;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AttendanceLeaveDatabase {
    private static final VietnameseCodeConverters.AttendanceStatusConverter ATTENDANCE_STATUS = new VietnameseCodeConverters.AttendanceStatusConverter();
    private static final VietnameseCodeConverters.AttendanceSourceConverter ATTENDANCE_SOURCE = new VietnameseCodeConverters.AttendanceSourceConverter();
    private static final VietnameseCodeConverters.LeaveTypeConverter LEAVE_TYPE = new VietnameseCodeConverters.LeaveTypeConverter();
    private static final VietnameseCodeConverters.LeaveStatusConverter LEAVE_STATUS = new VietnameseCodeConverters.LeaveStatusConverter();
    private final JdbcTemplate jdbc;

    public AttendanceLeaveDatabase(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private <T> List<T> read(String sql, RowMapper<T> mapper, Object... args) {
        return jdbc.query(sql,mapper,args);
    }
    private void command(String sql,Object... args) {
        try { jdbc.update(sql,args); } catch(DataAccessException error) { throw translate(error); }
    }
    private long create(String sql,Object... args) {
        try { return jdbc.queryForObject(sql,Long.class,args); } catch(DataAccessException error) { throw translate(error); }
    }
    public List<AttendanceLeaveDtos.AttendanceResponse> attendance(LocalDate date){
        String sql="SELECT maChamCong,maNhanVien,ngayLamViec,thoiDiemVaoCa,thoiDiemRaCa,trangThai,nguonDuLieu,maSuKienThietBi,ghiChu FROM dbo.vwChamCong";
        return date==null?read(sql+" ORDER BY ngayLamViec DESC,maNhanVien",this::attendanceRow):read(sql+" WHERE ngayLamViec=? ORDER BY ngayLamViec DESC,maNhanVien",this::attendanceRow,date);
    }
    public void importAttendance(String json,String actor){command("EXEC dbo.uspNhapChamCong ?,?",json,actor);}
    public List<AttendanceLeaveDtos.LeaveResponse> leaves(String status){
        String sql="SELECT maDonNghiPhep,maNhanVien,loaiNghiPhep,ngayBatDau,ngayKetThuc,lyDo,maNhanVienDoiCa,trangThai,nguoiYeuCau,nguoiPheDuyet,thoiDiemQuyetDinh,thoiDiemTao FROM dbo.vwDonNghiPhep";
        return status==null?read(sql+" ORDER BY thoiDiemTao DESC",this::leaveRow):read(sql+" WHERE trangThai=? ORDER BY thoiDiemTao DESC",this::leaveRow,status);
    }
    public long createLeave(AttendanceLeaveDtos.LeaveRequest request,String type,String actor){return create("EXEC dbo.uspTaoDonNghiPhep ?,?,?,?,?,?,?",request.employeeId(),type,request.startDate(),request.endDate(),request.reason().trim(),request.shiftSwapWith(),actor);}
    public void decideLeave(long id,boolean approve,String actor){command("EXEC dbo.uspQuyetDinhNghiPhep ?,?,?",id,approve,actor);}
    private AttendanceLeaveDtos.AttendanceResponse attendanceRow(java.sql.ResultSet rs, int n) throws java.sql.SQLException { return new AttendanceLeaveDtos.AttendanceResponse(rs.getLong(1), rs.getString(2), rs.getDate(3).toLocalDate(), ts(rs, 4), ts(rs, 5), ATTENDANCE_STATUS.convertToEntityAttribute(rs.getString(6)), ATTENDANCE_SOURCE.convertToEntityAttribute(rs.getString(7)), rs.getString(8), rs.getString(9)); }
    private AttendanceLeaveDtos.LeaveResponse leaveRow(java.sql.ResultSet rs, int n) throws java.sql.SQLException { return new AttendanceLeaveDtos.LeaveResponse(rs.getLong(1), rs.getString(2), LEAVE_TYPE.convertToEntityAttribute(rs.getString(3)), rs.getDate(4).toLocalDate(), rs.getDate(5).toLocalDate(), rs.getString(6), rs.getString(7), LEAVE_STATUS.convertToEntityAttribute(rs.getString(8)), rs.getString(9), rs.getString(10), ts(rs, 11), ts(rs, 12)); }
    private static LocalDateTime ts(java.sql.ResultSet rs, int index) throws java.sql.SQLException { var value = rs.getTimestamp(index); return value == null ? null : value.toLocalDateTime(); }
    private RuntimeException translate(DataAccessException error) {
        for(Throwable cause=error;cause!=null;cause=cause.getCause()) {
            if(cause instanceof SQLException sql) {
                String code=switch(sql.getErrorCode()) {
                    case 51204 -> "LEAVE_NOT_PENDING";
                    case 51208 -> "INVALID_LEAVE_PERIOD";
                    default -> null;
                };
                if(code!=null) return new DomainException(code,sql.getMessage());
            }
        }
        return error;
    }
}
