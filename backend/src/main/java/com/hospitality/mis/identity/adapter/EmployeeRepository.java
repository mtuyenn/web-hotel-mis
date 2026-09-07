package com.hospitality.mis.identity.adapter;

import com.hospitality.mis.identity.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Canonical persistence adapter for the employees account aggregate.
 *
 * <p>Identity application/security and reservation depend on this canonical
 * Employee aggregate.</p>
 */
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Employee> findForUpdateByEmployeeId(String employeeId);
}
