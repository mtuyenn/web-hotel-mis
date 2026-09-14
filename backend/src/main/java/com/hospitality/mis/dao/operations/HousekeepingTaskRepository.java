package com.hospitality.mis.dao.operations;

import com.hospitality.mis.entity.operations.HousekeepingTask;
import com.hospitality.mis.entity.operations.HousekeepingTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, Long> {
    List<HousekeepingTask> findByRoomIdOrderByUpdatedAtDesc(String roomId);
    List<HousekeepingTask> findByAssigneeAndStatusOrderByUpdatedAtDesc(String assignee, HousekeepingTaskStatus status);
}
