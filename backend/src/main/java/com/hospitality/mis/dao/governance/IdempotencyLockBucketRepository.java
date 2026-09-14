package com.hospitality.mis.dao.governance;

import com.hospitality.mis.entity.governance.IdempotencyLockBucket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyLockBucketRepository extends JpaRepository<IdempotencyLockBucket, Short> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bucket from IdempotencyLockBucket bucket where bucket.id = :id")
    Optional<IdempotencyLockBucket> lock(@Param("id") short id);
}
