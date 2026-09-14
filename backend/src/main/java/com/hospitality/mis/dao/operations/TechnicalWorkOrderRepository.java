package com.hospitality.mis.dao.operations;

import com.hospitality.mis.entity.operations.TechnicalWorkOrder;
import com.hospitality.mis.entity.operations.TechnicalWorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TechnicalWorkOrderRepository extends JpaRepository<TechnicalWorkOrder, Long> {
    List<TechnicalWorkOrder> findByRoomIdOrderByUpdatedAtDesc(String roomId);
    List<TechnicalWorkOrder> findByStatusOrderByUpdatedAtDesc(TechnicalWorkOrderStatus status);
}
