package com.hospitality.mis.dao.operations;
import com.hospitality.mis.entity.operations.HousekeepingChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HousekeepingChecklistTemplateRepository extends JpaRepository<HousekeepingChecklistTemplate, Long> { List<HousekeepingChecklistTemplate> findByActiveTrueOrderByNameAsc(); }
