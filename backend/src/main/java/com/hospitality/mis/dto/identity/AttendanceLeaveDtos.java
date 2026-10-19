package com.hospitality.mis.dto.identity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AttendanceLeaveDtos {
    private AttendanceLeaveDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record AttendanceResponse(Long id, String employeeId, LocalDate workDate,
                                     LocalDateTime clockIn, LocalDateTime clockOut,
                                     String status, String source, String deviceEventId, String note) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record AttendanceInput(@NotBlank String employeeId, @NotNull LocalDate workDate,
                                  LocalDateTime clockIn, LocalDateTime clockOut,
                                  @NotBlank String status, String source, String deviceEventId, String note) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record AttendanceImportRequest(@NotEmpty List<@Valid AttendanceInput> records) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record LeaveResponse(Long id, String employeeId, String leaveType, LocalDate startDate,
                                LocalDate endDate, String reason, String shiftSwapWith,
                                String status, String requestedBy, String approver,
                                LocalDateTime decidedAt, LocalDateTime createdAt) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record LeaveRequest(@NotBlank String employeeId, @NotBlank String leaveType,
                               @NotNull LocalDate startDate, @NotNull LocalDate endDate,
                               @NotBlank String reason, String shiftSwapWith) {}

}
