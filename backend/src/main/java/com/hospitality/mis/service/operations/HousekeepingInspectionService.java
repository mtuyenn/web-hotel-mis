package com.hospitality.mis.service.operations;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.HousekeepingInspectionRepository;
import com.hospitality.mis.dao.operations.HousekeepingTaskRepository;
import com.hospitality.mis.dto.operations.HousekeepingInspectionDtos;
import com.hospitality.mis.entity.operations.HousekeepingInspection;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;
@Service
public class HousekeepingInspectionService {
    private final HousekeepingInspectionRepository inspections; private final HousekeepingTaskRepository tasks; private final AuditService audit; private final Clock clock;
    public HousekeepingInspectionService(HousekeepingInspectionRepository inspections, HousekeepingTaskRepository tasks, AuditService audit, Clock clock) { this.inspections = inspections; this.tasks = tasks; this.audit = audit; this.clock = clock; }
    @Transactional public HousekeepingInspectionDtos.Response add(Long taskId, HousekeepingInspectionDtos.Request request, String actor) {
        var task = tasks.findById(taskId).orElseThrow(() -> new DomainException("HOUSEKEEPING_TASK_NOT_FOUND", "Không tìm thấy task dọn phòng"));
        var inspection = new HousekeepingInspection(); inspection.setTask(task); inspection.setInspectionType(request.inspectionType()); inspection.setItem(request.item().trim()); inspection.setQuantity(request.quantity()); inspection.setItemCondition(request.itemCondition()); inspection.setNote(request.note()); inspection.setCompletedBy(actor); inspection.setCompletedAt(LocalDateTime.now(clock));
        inspection = inspections.save(inspection); audit.record(actor, "HOUSEKEEPING_INSPECTION_RECORDED", "HOUSEKEEPING_TASK", taskId.toString(), null, request.inspectionType().name() + ":" + request.item().trim(), null); return HousekeepingInspectionDtos.Response.from(inspection);
    }
    @Transactional(readOnly = true) public List<HousekeepingInspectionDtos.Response> list(Long taskId) {
        if (!tasks.existsById(taskId)) throw new DomainException("HOUSEKEEPING_TASK_NOT_FOUND", "Không tìm thấy task dọn phòng");
        return inspections.findByTaskIdOrderByCompletedAtDescIdDesc(taskId).stream().map(HousekeepingInspectionDtos.Response::from).toList();
    }
}
