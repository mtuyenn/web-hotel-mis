package com.hospitality.mis.dao.operations;

import com.hospitality.mis.entity.operations.MaintenanceWorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceWorkOrderRepository extends JpaRepository<MaintenanceWorkOrder, String> {
    List<MaintenanceWorkOrder> findByRoomIdOrderByScheduledDateDesc(String roomId);
}
