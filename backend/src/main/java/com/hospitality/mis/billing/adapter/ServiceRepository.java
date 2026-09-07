package com.hospitality.mis.billing.adapter;

import com.hospitality.mis.billing.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Service> findWithLockById(String id);
}
