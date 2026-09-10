package com.hospitality.mis.dao.governance;



import com.hospitality.mis.entity.governance.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;



public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    java.util.List<AuditLog> findTop100ByOrderByCreatedAtDesc();
    java.util.List<AuditLog> findTop100ByActorOrderByCreatedAtDesc(String actor);
}
