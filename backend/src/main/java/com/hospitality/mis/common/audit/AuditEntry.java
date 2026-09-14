package com.hospitality.mis.common.audit;

import com.hospitality.mis.common.actor.ActorId;
import java.time.Instant;
import java.util.Objects;

/** Mô tả chuẩn, độc lập với phương thức truyền tải, về một thao tác có thể kiểm toán. */
public record AuditEntry(ActorId actor, String action, String entityType, String entityId,
                         /* Dữ liệu trước và sau được giữ dạng chuỗi để không phụ thuộc transport. */
                         String beforeData, String afterData, String reason, Instant occurredAt) {
    /** Bảo đảm các thông tin định danh của sự kiện audit luôn có giá trị hợp lệ. */
    public AuditEntry {
        actor = Objects.requireNonNull(actor, "actor must not be null");
        action = required(action, "action");
        entityType = required(entityType, "entity type");
        entityId = required(entityId, "entity id");
        occurredAt = Objects.requireNonNull(occurredAt, "occurred at must not be null");
    }

    /** Tạo một bản ghi audit với thời điểm hiện tại của server. */
    public static AuditEntry now(ActorId actor, String action, String entityType, String entityId,
                                 String beforeData, String afterData, String reason) {
        return new AuditEntry(actor, action, entityType, entityId, beforeData, afterData, reason, Instant.now());
    }

    /** Kiểm tra trường bắt buộc sau khi loại bỏ khoảng trắng ở đầu và cuối. */
    private static String required(String value, String field) {
        value = Objects.requireNonNull(value, field + " must not be null").trim();
        if (value.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
}
