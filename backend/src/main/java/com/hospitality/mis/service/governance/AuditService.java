package com.hospitality.mis.service.governance;





import com.hospitality.mis.dao.governance.AuditLogRepository;

import com.hospitality.mis.entity.governance.AuditLog;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.Clock;
import java.time.Instant;



/** Ghi và đọc nhật ký nghiệp vụ trong cùng transaction với thao tác gọi. */
@Service

public class AuditService {

    /** Kho audit; record bắt buộc tham gia transaction đang mở. */
    private final AuditLogRepository repository;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

    public AuditService(AuditLogRepository repository) { this.repository = repository; }

    @org.springframework.beans.factory.annotation.Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }



    /** Ghi audit không có correlation key, yêu cầu caller đã mở transaction. */
    @Transactional(propagation = Propagation.MANDATORY)

    public void record(String actor, String action, String entityType, String entityId,

                       String beforeData, String afterData, String reason) {

        record(actor, action, entityType, entityId, beforeData, afterData, reason, null);
    }

    /** Ghi actor, action, entity và before/after data cùng correlation key tùy chọn. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String actor, String action, String entityType, String entityId,

                       String beforeData, String afterData, String reason, String correlationKey) {

        repository.save(new AuditLog(actor == null || actor.isBlank() ? "SYSTEM" : actor, action,
                entityType, entityId, beforeData, afterData, reason, correlationKey, Instant.now(clock)));

    }

    /** Trả tối đa 100 bản ghi mới nhất theo phạm vi actor hoặc toàn hệ thống. */
    @Transactional(readOnly = true)
    public List<AuditLog> list(String actor, boolean global) {
        return global ? repository.findTop100ByOrderByCreatedAtDesc() : repository.findTop100ByActorOrderByCreatedAtDesc(actor);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> timeline(String entityType, String entityId) {
        return repository.findTop100ByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLog> page(String actor, boolean global, String action, String entityType, int page, int size) {
        int safeSize = Math.max(1, Math.min(100, size));
        return repository.search(global ? null : actor, action, entityType,
                org.springframework.data.domain.PageRequest.of(Math.max(0, page), safeSize));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLog> page(String actor, boolean global, String action, String entityType,
                                                                String entityId, String correlationKey, Instant from, Instant to, int page, int size) {
        int safeSize = Math.max(1, Math.min(100, size));
        return repository.searchAdvanced(global ? null : actor, action, entityType, entityId, correlationKey, from, to,
                org.springframework.data.domain.PageRequest.of(Math.max(0, page), safeSize));
    }

}
