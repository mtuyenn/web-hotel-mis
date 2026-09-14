package com.hospitality.mis.dao.operations;

import com.hospitality.mis.entity.operations.MaintenanceWorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Kho lệnh bảo trì theo phòng và ngày dự kiến. */
public interface MaintenanceWorkOrderRepository extends JpaRepository<MaintenanceWorkOrder, String> {
    /** Lấy lệnh bảo trì của phòng theo ngày dự kiến giảm dần. */
    List<MaintenanceWorkOrder> findByRoomIdOrderByScheduledDateDesc(String roomId);
}
