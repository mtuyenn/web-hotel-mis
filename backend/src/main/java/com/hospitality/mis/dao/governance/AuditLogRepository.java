package com.hospitality.mis.dao.governance;



import com.hospitality.mis.entity.governance.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;



/** Kho nhật ký audit phục vụ truy vấn toàn hệ thống hoặc theo actor. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /** Lấy tối đa 100 sự kiện audit toàn hệ thống mới nhất. */
    java.util.List<AuditLog> findTop100ByOrderByCreatedAtDesc();

    /** Lấy tối đa 100 sự kiện audit gần nhất của một actor. */
    java.util.List<AuditLog> findTop100ByActorOrderByCreatedAtDesc(String actor);
}
