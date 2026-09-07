package com.hospitality.mis.governance.adapter;

import com.hospitality.mis.governance.domain.ApprovalRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<ApprovalRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ApprovalRequest> findWithLockById(Long id);
    Optional<ApprovalRequest> findFirstByActionAndTargetIdAndStatusAndRequesterNotOrderByIdDesc(
            String action, String targetId, String status, String requester);
}
