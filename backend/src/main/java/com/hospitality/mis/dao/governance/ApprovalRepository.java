package com.hospitality.mis.dao.governance;

import com.hospitality.mis.entity.governance.ApprovalRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByStatusOrderByIdDesc(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ApprovalRequest> findWithLockById(Long id);

    @Query("""
            select a from ApprovalRequest a
            where a.action = :action and a.targetId = :targetId
              and a.requester = :actor and a.status = 'APPROVED'
              and a.consumedAt is null and a.payloadFingerprint = :payloadFingerprint
              and ((:amount is null and a.amount is null) or a.amount = :amount)
            order by a.id desc
            """)
    Optional<ApprovalRequest> findApprovedForBinding(@Param("action") String action,
                                                      @Param("targetId") String targetId,
                                                      @Param("actor") String actor,
                                                      @Param("payloadFingerprint") String payloadFingerprint,
                                                      @Param("amount") BigDecimal amount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select a from ApprovalRequest a
            where a.action = :action and a.targetId = :targetId
              and a.requester = :actor and a.status = 'APPROVED'
              and a.consumedAt is null and a.payloadFingerprint = :payloadFingerprint
              and ((:amount is null and a.amount is null) or a.amount = :amount)
            order by a.id desc
            """)
    Optional<ApprovalRequest> findApprovedForBindingWithLock(@Param("action") String action,
                                                              @Param("targetId") String targetId,
                                                              @Param("actor") String actor,
                                                              @Param("payloadFingerprint") String payloadFingerprint,
                                                              @Param("amount") BigDecimal amount);

    List<ApprovalRequest> findByStatusAndExpiresAtLessThanEqual(String status, Instant now);
}
