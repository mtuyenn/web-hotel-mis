package com.hospitality.mis.service.governance;





import com.hospitality.mis.dao.governance.AuditLogRepository;

import com.hospitality.mis.entity.governance.AuditLog;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;



@Service

public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) { this.repository = repository; }



    @Transactional(propagation = Propagation.MANDATORY)

    public void record(String actor, String action, String entityType, String entityId,

                       String beforeData, String afterData, String reason) {

        record(actor, action, entityType, entityId, beforeData, afterData, reason, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String actor, String action, String entityType, String entityId,

                       String beforeData, String afterData, String reason, String correlationKey) {

        repository.save(new AuditLog(actor == null || actor.isBlank() ? "SYSTEM" : actor, action,

                entityType, entityId, beforeData, afterData, reason, correlationKey));

    }

    @Transactional(readOnly = true)
    public List<AuditLog> list(String actor, boolean global) {
        return global ? repository.findTop100ByOrderByCreatedAtDesc() : repository.findTop100ByActorOrderByCreatedAtDesc(actor);
    }

}
