package com.hospitality.mis.governance.adapter;

import com.hospitality.mis.governance.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
