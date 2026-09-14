package com.hospitality.mis.service.governance;

import com.hospitality.mis.dao.governance.NotificationOutboxRepository;
import com.hospitality.mis.dto.governance.NotificationDtos;
import com.hospitality.mis.entity.governance.NotificationOutbox;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationOutboxService {
    private final NotificationOutboxRepository repository;
    public NotificationOutboxService(NotificationOutboxRepository repository) { this.repository = repository; }

    @Transactional
    public NotificationDtos.Response enqueue(String topic, String recipientRole, String payload, String dedupeKey) {
        var existing = repository.findByDedupeKey(dedupeKey);
        if (existing.isPresent()) return toResponse(existing.get());
        var event = new NotificationOutbox(); event.setTopic(topic); event.setRecipientRole(recipientRole); event.setPayload(payload);
        event.setDedupeKey(dedupeKey); event.setStatus(NotificationOutbox.Status.PENDING); event.setAvailableAt(LocalDateTime.now()); event.setCreatedAt(LocalDateTime.now());
        return toResponse(repository.save(event));
    }

    @Transactional(readOnly = true)
    public List<NotificationDtos.Response> poll(String role) {
        return repository.findTop100ByStatusAndAvailableAtLessThanEqualOrderByIdAsc(NotificationOutbox.Status.PENDING, LocalDateTime.now()).stream()
                .filter(x -> role == null || role.equals(x.getRecipientRole())).map(this::toResponse).toList();
    }

    @Transactional
    public NotificationDtos.Response markDelivered(Long id) {
        var event = repository.findById(id).orElseThrow(); event.setStatus(NotificationOutbox.Status.DELIVERED); event.setDeliveredAt(LocalDateTime.now()); return toResponse(event);
    }
    private NotificationDtos.Response toResponse(NotificationOutbox x) { return new NotificationDtos.Response(x.getId(), x.getTopic(), x.getRecipientRole(), x.getPayload(), x.getStatus().name(), x.getDedupeKey(), x.getAvailableAt(), x.getCreatedAt(), x.getDeliveredAt()); }
}
