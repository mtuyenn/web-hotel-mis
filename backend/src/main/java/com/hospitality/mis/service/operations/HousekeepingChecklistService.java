package com.hospitality.mis.service.operations;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.*;
import com.hospitality.mis.dto.operations.HousekeepingChecklistDtos;
import com.hospitality.mis.entity.operations.*;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class HousekeepingChecklistService {
    private final HousekeepingChecklistTemplateRepository templates; private final HousekeepingChecklistResultRepository results; private final HousekeepingTaskRepository tasks; private final AuditService audit;
    public HousekeepingChecklistService(HousekeepingChecklistTemplateRepository templates, HousekeepingChecklistResultRepository results, HousekeepingTaskRepository tasks, AuditService audit) { this.templates = templates; this.results = results; this.tasks = tasks; this.audit = audit; }
    @Transactional(readOnly = true) public List<HousekeepingChecklistDtos.TemplateResponse> templates() { return templates.findByActiveTrueOrderByNameAsc().stream().map(x -> new HousekeepingChecklistDtos.TemplateResponse(x.getId(), x.getName(), x.isActive())).toList(); }
    @Transactional public HousekeepingChecklistDtos.TemplateResponse createTemplate(HousekeepingChecklistDtos.TemplateRequest request, String actor) { var x = new HousekeepingChecklistTemplate(); x.setName(request.name()); templates.save(x); audit.record(actor, "HOUSEKEEPING_CHECKLIST_TEMPLATE_CREATED", "HOUSEKEEPING_CHECKLIST_TEMPLATE", "new", null, request.name(), null); return new HousekeepingChecklistDtos.TemplateResponse(x.getId(), x.getName(), x.isActive()); }
    @Transactional public HousekeepingChecklistDtos.ResultResponse addResult(Long taskId, HousekeepingChecklistDtos.ResultRequest request, String actor) { var task = tasks.findById(taskId).orElseThrow(() -> new DomainException("HOUSEKEEPING_TASK_NOT_FOUND", "Không tìm thấy task dọn phòng")); var x = new HousekeepingChecklistResult(); x.setTask(task); x.setItem(request.item()); x.setPassed(request.passed()); x.setNote(request.note()); x.setCompletedBy(actor); x.setCompletedAt(LocalDateTime.now()); results.save(x); audit.record(actor, "HOUSEKEEPING_CHECKLIST_RESULT_RECORDED", "HOUSEKEEPING_TASK", taskId.toString(), null, request.item(), null); return toResponse(x); }
    @Transactional(readOnly = true) public List<HousekeepingChecklistDtos.ResultResponse> results(Long taskId) { return results.findByTaskIdOrderByIdAsc(taskId).stream().map(this::toResponse).toList(); }
    private HousekeepingChecklistDtos.ResultResponse toResponse(HousekeepingChecklistResult x) { return new HousekeepingChecklistDtos.ResultResponse(x.getId(), x.getTask().getId(), x.getItem(), x.isPassed(), x.getNote(), x.getCompletedBy(), x.getCompletedAt()); }
}
