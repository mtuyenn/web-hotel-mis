package com.hospitality.mis.service.governance;

import com.hospitality.mis.dao.governance.AuditLogRepository;
import com.hospitality.mis.entity.governance.AuditLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Bản ghi từ chối vẫn được giữ lại khi giao dịch nghiệp vụ bị từ chối được hoàn tác. */
@Service
public class SecurityAuditService {
    /** Kho audit độc lập để ghi cả các request bị từ chối. */
    private final AuditLogRepository logs;
    public SecurityAuditService(AuditLogRepository logs) { this.logs = logs; }

    /** Ghi audit HTTP trong transaction mới để không mất dấu khi nghiệp vụ rollback. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String action, String method, String path, int status) {
        logs.save(new AuditLog(limit(actor, 50), action, "HTTP_REQUEST", limit(path, 100),
                null, Integer.toString(status), method, null));
    }

    /** Giới hạn dữ liệu request trước khi lưu để bảo vệ kích thước nhật ký. */
    private String limit(String value, int size) {
        return value.substring(0, Math.min(size, value.length()));
    }
}
