package com.hospitality.mis.dao.operations;
import com.hospitality.mis.entity.operations.HousekeepingChecklistResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HousekeepingChecklistResultRepository extends JpaRepository<HousekeepingChecklistResult, Long> { List<HousekeepingChecklistResult> findByTaskIdOrderByIdAsc(Long taskId); }
