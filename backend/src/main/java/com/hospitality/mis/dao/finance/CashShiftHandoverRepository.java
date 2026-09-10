package com.hospitality.mis.dao.finance;

import com.hospitality.mis.entity.finance.CashShiftHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CashShiftHandoverRepository extends JpaRepository<CashShiftHandover, Long> {
    List<CashShiftHandover> findByShiftCodeOrderByHandedOverAtDesc(String shiftCode);
    List<CashShiftHandover> findAllByOrderByHandedOverAtDesc();
    Optional<CashShiftHandover> findFirstByFromActorOrderByHandedOverAtDesc(String fromActor);
}
