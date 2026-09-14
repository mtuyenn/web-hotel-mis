package com.hospitality.mis.dao.identity;
import com.hospitality.mis.entity.identity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import com.hospitality.mis.entity.identity.EmployeeShift.Status;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {
    List<EmployeeShift> findByShiftDateOrderByStartsAtAsc(LocalDate date);
    List<EmployeeShift> findByEmployeeEmployeeIdAndShiftDateOrderByStartsAtAsc(String employeeId, LocalDate date);
    List<EmployeeShift> findByShiftDateBetweenOrderByShiftDateAscStartsAtAsc(LocalDate from, LocalDate to);
    List<EmployeeShift> findByEmployeeEmployeeIdAndShiftDateBetweenOrderByShiftDateAscStartsAtAsc(String employeeId, LocalDate from, LocalDate to);
    @Query("select count(s) > 0 from EmployeeShift s where s.employee.employeeId = :employeeId and s.status <> :cancelled and s.startsAt < :endsAt and s.endsAt > :startsAt")
    boolean hasOverlap(@Param("employeeId") String employeeId, @Param("startsAt") LocalDateTime startsAt,
                       @Param("endsAt") LocalDateTime endsAt, @Param("cancelled") Status cancelled);
    long countByShiftDateAndShiftCodeAndStatusNot(LocalDate date, String shiftCode, Status status);
}
