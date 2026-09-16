package com.hospitality.mis.service.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.HousekeepingChecklistResultRepository;
import com.hospitality.mis.dao.operations.HousekeepingChecklistTemplateRepository;
import com.hospitality.mis.dao.operations.HousekeepingTaskRepository;
import com.hospitality.mis.dto.operations.HousekeepingChecklistDtos;
import com.hospitality.mis.entity.operations.HousekeepingChecklistResult;
import com.hospitality.mis.entity.operations.HousekeepingChecklistTemplate;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class HousekeepingChecklistService {
    private final HousekeepingChecklistTemplateRepository templates;
    private final HousekeepingChecklistResultRepository results;
    private final HousekeepingTaskRepository tasks;
    private final AuditService audit;
    private final Clock clock;

    public HousekeepingChecklistService(HousekeepingChecklistTemplateRepository templates,
                                        HousekeepingChecklistResultRepository results,
                                        HousekeepingTaskRepository tasks, AuditService audit, Clock clock) {
        this.templates = templates;
        this.results = results;
        this.tasks = tasks;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<HousekeepingChecklistDtos.TemplateResponse> templates() {
        return templates.findByActiveTrueOrderByNameAsc().stream()
                .map(x -> new HousekeepingChecklistDtos.TemplateResponse(x.getId(), x.getName(), x.isActive())).toList();
    }

    @Transactional
    public HousekeepingChecklistDtos.TemplateResponse createTemplate(HousekeepingChecklistDtos.TemplateRequest request,
                                                                      String actor) {
        var template = new HousekeepingChecklistTemplate();
        template.setName(request.name().trim());
        templates.save(template);
        audit.record(actor, "HOUSEKEEPING_CHECKLIST_TEMPLATE_CREATED", "HOUSEKEEPING_CHECKLIST_TEMPLATE",
                "new", null, template.getName(), null);
        return new HousekeepingChecklistDtos.TemplateResponse(template.getId(), template.getName(), template.isActive());
    }

    @Transactional
    public HousekeepingChecklistDtos.ResultResponse addResult(Long taskId,
                                                               HousekeepingChecklistDtos.ResultRequest request,
                                                               String actor) {
        var task = tasks.findForUpdateById(taskId).orElseThrow(() ->
                new DomainException("HOUSEKEEPING_TASK_NOT_FOUND", "Không tìm thấy task dọn phòng"));
        String item = request.item().trim();
        var activeTemplates = templates.findByActiveTrueOrderByNameAsc();
        if (activeTemplates.stream().noneMatch(template -> template.getName().equals(item)))
            throw new DomainException("HOUSEKEEPING_CHECKLIST_ITEM_NOT_ACTIVE",
                    "Checklist item không tồn tại hoặc đã ngừng dùng");

        var result = new HousekeepingChecklistResult();
        result.setTask(task);
        result.setItem(item);
        result.setPassed(request.passed());
        result.setNote(request.note());
        result.setCompletedBy(actor);
        result.setCompletedAt(LocalDateTime.now(clock));
        results.save(result);

        var latest = new HashMap<String, Boolean>();
        results.findByTaskIdOrderByIdAsc(taskId)
                .forEach(existing -> latest.put(existing.getItem(), existing.isPassed()));
        latest.put(item, result.isPassed());
        task.setChecklistComplete(activeTemplates.stream()
                .allMatch(template -> Boolean.TRUE.equals(latest.get(template.getName()))));
        task.setUpdatedAt(LocalDateTime.now(clock));

        audit.record(actor, "HOUSEKEEPING_CHECKLIST_RESULT_RECORDED", "HOUSEKEEPING_TASK",
                taskId.toString(), null, item, null);
        return toResponse(result);
    }

    @Transactional(readOnly = true)
    public List<HousekeepingChecklistDtos.ResultResponse> results(Long taskId) {
        return results.findByTaskIdOrderByIdAsc(taskId).stream().map(this::toResponse).toList();
    }

    private HousekeepingChecklistDtos.ResultResponse toResponse(HousekeepingChecklistResult result) {
        return new HousekeepingChecklistDtos.ResultResponse(result.getId(), result.getTask().getId(), result.getItem(),
                result.isPassed(), result.getNote(), result.getCompletedBy(), result.getCompletedAt());
    }
}
