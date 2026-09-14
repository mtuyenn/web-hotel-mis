package com.hospitality.mis.service.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.HousekeepingTaskRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.operations.HousekeepingDtos;
import com.hospitality.mis.entity.operations.HousekeepingTask;
import com.hospitality.mis.entity.operations.HousekeepingTaskStatus;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousekeepingService {
    private final HousekeepingTaskRepository tasks;
    private final RoomRepository rooms;
    private final AuditService audit;

    public HousekeepingService(HousekeepingTaskRepository tasks, RoomRepository rooms, AuditService audit) {
        this.tasks = tasks; this.rooms = rooms; this.audit = audit;
    }

    @Transactional
    public HousekeepingDtos.Response create(HousekeepingDtos.CreateRequest request, String actor) {
        var room = rooms.findForUpdate(request.roomId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        var task = new HousekeepingTask(); task.setRoom(room); task.setAssignee(request.assignee());
        task.setAssignedBy(actor); task.setNote(request.note()); task.setStatus(HousekeepingTaskStatus.NEEDS_CLEANING);
        task.setUpdatedAt(LocalDateTime.now()); room.setStatus(RoomStatus.CLEANING);
        tasks.save(task); audit.record(actor, "HOUSEKEEPING_TASK_CREATED", "HOUSEKEEPING_TASK", "new", null, request.roomId(), null);
        return toResponse(task);
    }

    @Transactional
    public HousekeepingDtos.Response update(Long id, HousekeepingDtos.UpdateRequest request, String actor) {
        var task = tasks.findById(id).orElseThrow(() -> new DomainException("HOUSEKEEPING_TASK_NOT_FOUND", "Không tìm thấy task dọn phòng"));
        HousekeepingTaskStatus next;
        try { next = HousekeepingTaskStatus.valueOf(request.status().trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new DomainException("INVALID_HOUSEKEEPING_STATUS", "Trạng thái dọn phòng không hợp lệ"); }
        if (!task.getStatus().canTransitionTo(next)) throw new DomainException("INVALID_HOUSEKEEPING_TRANSITION", "Chuyển trạng thái dọn phòng không hợp lệ");
        if (request.assignee() != null) task.setAssignee(request.assignee());
        if (request.checklistComplete() != null) task.setChecklistComplete(request.checklistComplete());
        if (request.blockingIncident() != null) task.setBlockingIncident(request.blockingIncident());
        if (request.note() != null) task.setNote(request.note());
        if (next == HousekeepingTaskStatus.READY && (!task.isChecklistComplete() || task.isBlockingIncident()))
            throw new DomainException("HOUSEKEEPING_CHECKLIST_REQUIRED", "Phòng chỉ READY sau khi hoàn thành checklist và không còn incident blocking");
        var before = task.getStatus(); task.setStatus(next); task.setUpdatedAt(LocalDateTime.now());
        var room = rooms.findForUpdate(task.getRoom().getId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        room.setStatus(next == HousekeepingTaskStatus.READY ? RoomStatus.READY
                : next == HousekeepingTaskStatus.WAITING_TECHNICAL ? RoomStatus.MAINTENANCE : RoomStatus.CLEANING);
        audit.record(actor, "HOUSEKEEPING_TASK_STATUS_CHANGED", "HOUSEKEEPING_TASK", id.toString(), before.name(), next.name(), null);
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<HousekeepingDtos.Response> list(String roomId, String assignee, HousekeepingTaskStatus status) {
        List<HousekeepingTask> data = roomId != null ? tasks.findByRoomIdOrderByUpdatedAtDesc(roomId)
                : assignee != null && status != null ? tasks.findByAssigneeAndStatusOrderByUpdatedAtDesc(assignee, status)
                : tasks.findAll();
        return data.stream().filter(x -> status == null || x.getStatus() == status).map(this::toResponse).toList();
    }

    private HousekeepingDtos.Response toResponse(HousekeepingTask t) {
        return new HousekeepingDtos.Response(t.getId(), t.getRoom().getId(), t.getAssignee(), t.getStatus().name(),
                t.isChecklistComplete(), t.isBlockingIncident(), t.getNote(), t.getAssignedBy(), t.getUpdatedAt());
    }
}
