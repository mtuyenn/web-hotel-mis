package com.hospitality.mis.service.identity;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.identity.AttendanceLeaveDatabase;
import com.hospitality.mis.dto.identity.AttendanceLeaveDtos;
import com.hospitality.mis.persistence.VietnameseCodeConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class AttendanceLeaveService {
    private static final VietnameseCodeConverters.AttendanceStatusConverter ATTENDANCE_STATUS = new VietnameseCodeConverters.AttendanceStatusConverter();
    private static final VietnameseCodeConverters.AttendanceSourceConverter ATTENDANCE_SOURCE = new VietnameseCodeConverters.AttendanceSourceConverter();
    private static final VietnameseCodeConverters.LeaveTypeConverter LEAVE_TYPE = new VietnameseCodeConverters.LeaveTypeConverter();
    private static final VietnameseCodeConverters.LeaveStatusConverter LEAVE_STATUS = new VietnameseCodeConverters.LeaveStatusConverter();
    private final AttendanceLeaveDatabase database;

    public AttendanceLeaveService(AttendanceLeaveDatabase database) {
        this.database = database;
    }

    @Transactional(readOnly = true)
    public List<AttendanceLeaveDtos.AttendanceResponse> attendance(LocalDate date) {
        return database.attendance(date);
    }

    public List<AttendanceLeaveDtos.AttendanceResponse> importAttendance(AttendanceLeaveDtos.AttendanceImportRequest request, String actor) {
        var records=new java.util.ArrayList<LinkedHashMap<String,Object>>();
        for (var row : request.records()) {
            var data=new LinkedHashMap<String,Object>();
            data.put("maNhanVien",row.employeeId()); data.put("ngay",row.workDate().toString());
            data.put("vao",row.clockIn()==null?null:row.clockIn().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            data.put("ra",row.clockOut()==null?null:row.clockOut().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            data.put("trangThai",attendanceStatus(row.status()));data.put("nguon",attendanceSource(row.source()));
            data.put("suKien",row.deviceEventId());data.put("ghiChu",row.note()); records.add(data);
        }
        try { database.importAttendance(new ObjectMapper().writeValueAsString(records),actor); }
        catch(com.fasterxml.jackson.core.JsonProcessingException error) { throw new IllegalStateException("Cannot encode attendance command",error); }
        return attendance(null);
    }

    private static String attendanceStatus(String value) {
        try {
            return ATTENDANCE_STATUS.convertToDatabaseColumn(code(value));
        } catch (IllegalArgumentException exception) {
            throw new DomainException(
                    "INVALID_ATTENDANCE_STATUS",
                    "Trạng thái chấm công không hợp lệ; dùng PRESENT, LATE, ABSENT hoặc ON_LEAVE"
            );
        }
    }

    private static String attendanceSource(String value) {
        try {
            return ATTENDANCE_SOURCE.convertToDatabaseColumn(value == null ? "MANUAL" : code(value));
        } catch (IllegalArgumentException exception) {
            throw new DomainException(
                    "INVALID_ATTENDANCE_SOURCE",
                    "Nguồn chấm công không hợp lệ; dùng MANUAL hoặc BIOMETRIC_IMPORT"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<AttendanceLeaveDtos.LeaveResponse> leaves(String status) {
        return database.leaves(status==null||status.isBlank()?null:LEAVE_STATUS.convertToDatabaseColumn(code(status)));
    }

    public AttendanceLeaveDtos.LeaveResponse createLeave(AttendanceLeaveDtos.LeaveRequest request, String actor) {
        if (request.endDate().isBefore(request.startDate())) throw new DomainException("INVALID_LEAVE_PERIOD", "Khoảng nghỉ không hợp lệ");
        long id = database.createLeave(request,LEAVE_TYPE.convertToDatabaseColumn(code(request.leaveType())),actor);
        return leaves(null).stream().filter(row -> row.id().equals(id)).findFirst().orElseThrow();
    }

    public AttendanceLeaveDtos.LeaveResponse decideLeave(long id, boolean approve, String actor) {
        database.decideLeave(id,approve,actor);
        return leaves(null).stream().filter(row -> row.id().equals(id)).findFirst().orElseThrow();
    }

    private static String code(String value) { return value == null ? null : value.trim().toUpperCase(java.util.Locale.ROOT); }
}
