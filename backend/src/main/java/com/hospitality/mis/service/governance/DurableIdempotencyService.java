package com.hospitality.mis.service.governance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.governance.IdempotencyRecordRepository;
import com.hospitality.mis.dao.governance.IdempotencyLockBucketRepository;
import com.hospitality.mis.entity.governance.IdempotencyLockBucket;
import com.hospitality.mis.entity.governance.IdempotencyRecord;
import com.hospitality.mis.service.reservation.IdempotencySupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/** Persists idempotency state and the response in the same transaction as the command. */
@Service
public class DurableIdempotencyService {
    private final IdempotencyRecordRepository records;
    private final IdempotencyLockBucketRepository lockBuckets;
    private final ObjectMapper objectMapper;
    private final Clock businessClock;

    public DurableIdempotencyService(IdempotencyRecordRepository records, IdempotencyLockBucketRepository lockBuckets,
                                     ObjectMapper objectMapper, Clock businessClock) {
        this.records = records;
        this.lockBuckets = lockBuckets;
        this.objectMapper = objectMapper;
        this.businessClock = businessClock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public <T> T execute(String scope, String key, String actor, String requestHash,
                         Class<T> responseType, Supplier<T> command) {
        String normalizedKey = IdempotencySupport.requireKey(key);
        requireMetadata(scope, actor, requestHash, responseType, command);
        lockKey(scope, normalizedKey);
        var existing = records.findForUpdate(scope, normalizedKey);
        if (existing.isPresent()) return replay(existing.get(), actor, requestHash, responseType);

        IdempotencyRecord record = new IdempotencyRecord(scope, normalizedKey, actor, requestHash,
                LocalDateTime.now(businessClock));
        // Flush reserves the unique scope/key before any business side effect is executed.
        records.saveAndFlush(record);
        T result = command.get();
        try {
            record.complete(responseType.getName(), objectMapper.writeValueAsString(result),
                    LocalDateTime.now(businessClock));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Không thể lưu kết quả idempotency", exception);
        }
        return result;
    }

    /** Variant for JPA aggregates: persist a completion marker and reload the aggregate on retries. */
    @Transactional(propagation = Propagation.MANDATORY)
    public <T> T executeWithReplay(String scope, String key, String actor, String requestHash,
                                   Supplier<T> command, Supplier<T> replayLoader) {
        String normalizedKey = IdempotencySupport.requireKey(key);
        requireMetadata(scope, actor, requestHash, Object.class, command);
        Objects.requireNonNull(replayLoader, "replayLoader");
        lockKey(scope, normalizedKey);
        var existing = records.findForUpdate(scope, normalizedKey);
        if (existing.isPresent()) {
            validateExisting(existing.get(), actor, requestHash);
            return replayLoader.get();
        }
        IdempotencyRecord record = new IdempotencyRecord(scope, normalizedKey, actor, requestHash,
                LocalDateTime.now(businessClock));
        records.saveAndFlush(record);
        T result = command.get();
        record.complete("DB_REPLAY", "{}", LocalDateTime.now(businessClock));
        return result;
    }

    private <T> T replay(IdempotencyRecord record, String actor, String requestHash, Class<T> responseType) {
        validateExisting(record, actor, requestHash);
        if (!responseType.getName().equals(record.getResponseType())) {
            throw new DomainException("IDEMPOTENCY_KEY_CONFLICT", "Kiểu kết quả của Idempotency-Key không khớp");
        }
        try {
            return objectMapper.readValue(record.getResponseJson(), responseType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Không thể đọc kết quả idempotency đã lưu", exception);
        }
    }

    private void validateExisting(IdempotencyRecord record, String actor, String requestHash) {
        if (!record.getActor().equals(actor) || !record.getRequestHash().equals(requestHash)) {
            throw new DomainException("IDEMPOTENCY_KEY_CONFLICT",
                    "Idempotency key đã được dùng cho yêu cầu khác hoặc actor khác");
        }
        if (record.getStatus() != IdempotencyRecord.Status.COMPLETED || record.getResponseJson() == null) {
            throw new DomainException("IDEMPOTENCY_REQUEST_IN_PROGRESS", "Yêu cầu cùng Idempotency-Key đang được xử lý");
        }
    }

    private void requireMetadata(String scope, String actor, String requestHash, Class<?> responseType,
                                 Supplier<?> command) {
        if (scope == null || scope.isBlank() || scope.length() > 100) {
            throw new DomainException("INVALID_IDEMPOTENCY_SCOPE", "Idempotency scope không hợp lệ");
        }
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(requestHash, "requestHash");
        Objects.requireNonNull(responseType, "responseType");
        Objects.requireNonNull(command, "command");
    }

    private void lockKey(String scope, String key) {
        short bucket = (short) Math.floorMod(Objects.hash(scope, key), 64);
        if (lockBuckets.lock(bucket).isEmpty()) {
            // Hibernate create-drop test databases do not execute Flyway seed data.
            lockBuckets.saveAndFlush(new IdempotencyLockBucket(bucket));
            lockBuckets.lock(bucket);
        }
    }
}
