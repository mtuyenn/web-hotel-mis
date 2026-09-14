package com.hospitality.mis.dao.governance;



import com.hospitality.mis.entity.governance.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;



/** Kho nhật ký audit phục vụ truy vấn toàn hệ thống hoặc theo actor. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    java.util.List<AuditLog> findTop100ByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(String entityType, String entityId);
    @org.springframework.data.jpa.repository.Query("select a from AuditLog a where (:actor is null or a.actor = :actor) and (:action is null or a.action = :action) and (:entityType is null or a.entityType = :entityType) order by a.createdAt desc, a.id desc")
    org.springframework.data.domain.Page<AuditLog> search(@org.springframework.data.repository.query.Param("actor") String actor,
            @org.springframework.data.repository.query.Param("action") String action,
            @org.springframework.data.repository.query.Param("entityType") String entityType,
            org.springframework.data.domain.Pageable pageable);
    /** Lấy tối đa 100 sự kiện audit toàn hệ thống mới nhất. */
    java.util.List<AuditLog> findTop100ByOrderByCreatedAtDesc();

    /** Lấy tối đa 100 sự kiện audit gần nhất của một actor. */
    java.util.List<AuditLog> findTop100ByActorOrderByCreatedAtDesc(String actor);
}
