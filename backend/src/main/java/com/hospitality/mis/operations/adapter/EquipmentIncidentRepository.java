package com.hospitality.mis.operations.adapter;

import com.hospitality.mis.operations.domain.EquipmentIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipmentIncidentRepository extends JpaRepository<EquipmentIncident, Long> {
    List<EquipmentIncident> findByReservationId(Long reservationId);
}
