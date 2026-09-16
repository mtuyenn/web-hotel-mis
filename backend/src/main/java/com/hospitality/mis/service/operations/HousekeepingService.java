package com.hospitality.mis.service.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.HousekeepingTaskRepository;
import com.hospitality.mis.dao.operations.HousekeepingChecklistResultRepository;
import com.hospitality.mis.dao.operations.HousekeepingChecklistTemplateRepository;
import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.operations.HousekeepingDtos;
import com.hospitality.mis.entity.operations.HousekeepingTask;
import com.hospitality.mis.entity.operations.HousekeepingTaskStatus;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import com.hospitality.mis.entity.operations.IncidentHandoffStatus;
import com.hospitality.mis.entity.operations.IncidentSeverity;

@Service
public class HousekeepingService {
    private final HousekeepingTaskRepository tasks;
    private final RoomRepository rooms;
    private final AuditService audit;
    private final HousekeepingChecklistTemplateRepository templates;
    private final HousekeepingChecklistResultRepository results;
    private final EquipmentIncidentRepository incidents;
    private final Clock clock;

    public HousekeepingService(HousekeepingTaskRepository tasks, RoomRepository rooms, AuditService audit,
                               HousekeepingChecklistTemplateRepository templates,
                               HousekeepingChecklistResultRepository results,
                               EquipmentIncidentRepository incidents, Clock clock) {
        this.tasks = tasks; this.rooms = rooms; this.audit = audit; this.templates = templates;
        this.results = results; this.incidents = incidents; this.clock = clock;
    }

    @Transactional
    public HousekeepingDtos.Response create(HousekeepingDtos.CreateRequest request, String actor) {
        var room = rooms.findForUpdate(request.roomId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        var task = new HousekeepingTask(); task.setRoom(room); task.setAssignee(request.assignee());
        task.setAssignedBy(actor); task.setNote(request.note()); task.setStatus(HousekeepingTaskStatus.NEEDS_CLEANING);
        task.setUpdatedAt(LocalDateTime.now(clock)); room.setStatus(RoomStatus.CLEANING);
        tasks.save(task); audit.record(actor, "HOUSEKEEPING_TASK_CREATED", "HOUSEKEEPING_TASK", "new", null, request.roomId(), null);
        return toResponse(task);
    }

    @Transactional
    public HousekeepingDtos.Response update(Long id, HousekeepingDtos.UpdateRequest request, String actor) {
        var task = tasks.findForUpdateById(id).orElseThrow(() -> new DomainException("HOUSEKEEPING_TASK_NOT_FOUND", "Không tìm thấy task dọn phòng"));
        HousekeepingTaskStatus next;
        try { next = HousekeepingTaskStatus.valueOf(request.status().trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new DomainException("INVALID_HOUSEKEEPING_STATUS", "Trạng thái dọn phòng không hợp lệ"); }
        if (!task.getStatus().canTransitionTo(next)) throw new DomainException("INVALID_HOUSEKEEPING_TRANSITION", "Chuyển trạng thái dọn phòng không hợp lệ");
        if (!hasManagementRole() && !actor.equals(task.getAssignee()))
            throw new DomainException("HOUSEKEEPING_TASK_SCOPE_FORBIDDEN", "Chỉ người được phân công mới được cập nhật task");
        if (request.assignee() != null && !request.assignee().equals(task.getAssignee())) {
            if (!hasManagementRole())
                throw new DomainException("HOUSEKEEPING_ASSIGNMENT_FORBIDDEN", "Chỉ Manager mới được đổi người phụ trách task");
            task.setAssignee(request.assignee());
        }
        if (request.note() != null) task.setNote(request.note());
        var room = rooms.findForUpdate(task.getRoom().getId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        if (next == HousekeepingTaskStatus.READY) {
            boolean checklistComplete = hasPassedEveryActiveChecklist(task.getId());
            task.setChecklistComplete(checklistComplete);
            boolean blocking = task.isBlockingIncident() || incidents.existsByRoomIdAndSeverityInAndHandoffStatusNot(
                    room.getId(), List.of(IncidentSeverity.HIGH, IncidentSeverity.CRITICAL), IncidentHandoffStatus.RESOLVED);
            if (!checklistComplete || blocking)
                throw new DomainException("HOUSEKEEPING_CHECKLIST_REQUIRED", "Phòng chỉ READY sau khi hoàn thành checklist và không còn incident blocking");
            if (room.getStatus() == RoomStatus.MAINTENANCE)
                throw new DomainException("ROOM_MAINTENANCE_LOCKED", "Phòng đang bị maintenance khóa");
        }
        var before = task.getStatus(); task.setStatus(next); task.setUpdatedAt(LocalDateTime.now(clock));
        room.setStatus(next == HousekeepingTaskStatus.READY ? RoomStatus.READY
                : next == HousekeepingTaskStatus.WAITING_TECHNICAL ? RoomStatus.MAINTENANCE : RoomStatus.CLEANING);
        audit.record(actor, "HOUSEKEEPING_TASK_STATUS_CHANGED", "HOUSEKEEPING_TASK", id.toString(), before.name(), next.name(), null);
        return toResponse(task);
    }

    private boolean hasPassedEveryActiveChecklist(Long taskId) {
        var active = templates.findByActiveTrueOrderByNameAsc();
        if (active.isEmpty()) return false;
        var latest = new HashMap<String, Boolean>();
        results.findByTaskIdOrderByIdAsc(taskId).forEach(result -> latest.put(result.getItem(), result.isPassed()));
        return active.stream().allMatch(template -> Boolean.TRUE.equals(latest.get(template.getName())));
    }

    @Transactional(readOnly = true)
    public List<HousekeepingDtos.Response> list(String roomId, String assignee, HousekeepingTaskStatus status) {
        return tasks.findAll().stream()
                .filter(x -> roomId == null || roomId.equals(x.getRoom().getId()))
                .filter(x -> assignee == null || assignee.equals(x.getAssignee()))
                .filter(x -> status == null || x.getStatus() == status)
                .sorted(java.util.Comparator.comparing(HousekeepingTask::getUpdatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .map(this::toResponse).toList();
    }

    private HousekeepingDtos.Response toResponse(HousekeepingTask t) {
        return new HousekeepingDtos.Response(t.getId(), t.getRoom().getId(), t.getAssignee(), t.getStatus().name(),
                t.isChecklistComplete(), t.isBlockingIncident(), t.getNote(), t.getAssignedBy(), t.getUpdatedAt());
    }

    private boolean hasManagementRole() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DIRECTOR") || a.getAuthority().equals("ROLE_MANAGER"));
    }
}
