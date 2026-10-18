package com.hospitality.mis.service.identity;

import com.hospitality.mis.dto.operations.BackofficeDtos;
import com.hospitality.mis.service.operations.BackofficeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Attendance and leave use cases for the HR boundary. */
@Service
public class AttendanceLeaveService {
    private final BackofficeService backoffice;

    public AttendanceLeaveService(BackofficeService backoffice) { this.backoffice = backoffice; }
    public List<BackofficeDtos.AttendanceResponse> attendance(LocalDate date) { return backoffice.attendance(date); }
    public List<BackofficeDtos.AttendanceResponse> importAttendance(BackofficeDtos.AttendanceImportRequest request, String actor) { return backoffice.importAttendance(request, actor); }
    public List<BackofficeDtos.LeaveResponse> leaves(String status) { return backoffice.leaves(status); }
    public BackofficeDtos.LeaveResponse createLeave(BackofficeDtos.LeaveRequest request, String actor) { return backoffice.createLeave(request, actor); }
    public BackofficeDtos.LeaveResponse decideLeave(long id, boolean approve, String actor) { return backoffice.decideLeave(id, approve, actor); }
}
