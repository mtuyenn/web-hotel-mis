package com.hospitality.mis.dao.operations;
import com.hospitality.mis.entity.operations.HousekeepingInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HousekeepingInspectionRepository extends JpaRepository<HousekeepingInspection, Long> {
    List<HousekeepingInspection> findByTaskIdOrderByCompletedAtDescIdDesc(Long taskId);
}
