package com.hospitality.mis.operations.adapter;

import com.hospitality.mis.operations.domain.BaoTri;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceWorkOrderRepository extends JpaRepository<BaoTri, String> {
    List<BaoTri> findByRoomIdOrderByNgayBaoTriDesc(String roomId);
}
