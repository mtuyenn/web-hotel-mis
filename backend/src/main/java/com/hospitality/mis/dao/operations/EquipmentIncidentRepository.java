package com.hospitality.mis.dao.operations;



import com.hospitality.mis.entity.operations.EquipmentIncident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.hospitality.mis.entity.operations.IncidentHandoffStatus;
import com.hospitality.mis.entity.operations.IncidentSeverity;



/** Kho sự cố thiết bị liên kết với các đặt phòng. */
public interface EquipmentIncidentRepository extends JpaRepository<EquipmentIncident, Long> {

    /** Lấy các sự cố thiết bị gắn với một đặt phòng để theo dõi vận hành. */
    List<EquipmentIncident> findByReservationId(Long reservationId);
    boolean existsByRoomIdAndSeverityInAndHandoffStatusNot(String roomId, List<IncidentSeverity> severities,
                                                            IncidentHandoffStatus status);
}
