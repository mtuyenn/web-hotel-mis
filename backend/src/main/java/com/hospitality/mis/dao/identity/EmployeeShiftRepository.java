package com.hospitality.mis.dao.identity;
import com.hospitality.mis.entity.identity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {
    List<EmployeeShift> findByShiftDateOrderByStartsAtAsc(LocalDate date);
    List<EmployeeShift> findByEmployeeEmployeeIdAndShiftDateOrderByStartsAtAsc(String employeeId, LocalDate date);
}
