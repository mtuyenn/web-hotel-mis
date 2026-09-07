package com.hospitality.mis.governance.application;

import com.hospitality.mis.governance.adapter.AuditLogRepository;
import com.hospitality.mis.governance.domain.AuditLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repository;
    public AuditService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String actor, String action, String entityType, String entityId,
                       String beforeData, String afterData, String reason) {
        repository.save(new AuditLog(actor == null || actor.isBlank() ? "SYSTEM" : actor, action,
                entityType, entityId, beforeData, afterData, reason));
    }
}
