package com.hospitality.mis.dao.operations;

import com.hospitality.mis.entity.operations.HousekeepingTask;
import com.hospitality.mis.entity.operations.HousekeepingTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<HousekeepingTask> findForUpdateById(Long id);
    List<HousekeepingTask> findByRoomIdOrderByUpdatedAtDesc(String roomId);
    List<HousekeepingTask> findByAssigneeAndStatusOrderByUpdatedAtDesc(String assignee, HousekeepingTaskStatus status);
    Optional<HousekeepingTask> findFirstByRoomIdOrderByUpdatedAtDesc(String roomId);
}
