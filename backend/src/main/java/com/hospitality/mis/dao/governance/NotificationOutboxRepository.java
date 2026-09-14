package com.hospitality.mis.dao.governance;

import com.hospitality.mis.entity.governance.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    Optional<NotificationOutbox> findByDedupeKey(String dedupeKey);
    List<NotificationOutbox> findTop100ByStatusAndAvailableAtLessThanEqualOrderByIdAsc(NotificationOutbox.Status status, LocalDateTime now);
}
