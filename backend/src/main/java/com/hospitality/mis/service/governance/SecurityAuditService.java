package com.hospitality.mis.service.governance;

import com.hospitality.mis.dao.governance.AuditLogRepository;
import com.hospitality.mis.entity.governance.AuditLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Denials survive rollback of the rejected business transaction. */
@Service
public class SecurityAuditService {
    private final AuditLogRepository logs;
    public SecurityAuditService(AuditLogRepository logs) { this.logs = logs; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String action, String method, String path, int status) {
        logs.save(new AuditLog(limit(actor, 50), action, "HTTP_REQUEST", limit(path, 100),
                null, Integer.toString(status), method, null));
    }

    private String limit(String value, int size) {
        return value.substring(0, Math.min(size, value.length()));
    }
}
