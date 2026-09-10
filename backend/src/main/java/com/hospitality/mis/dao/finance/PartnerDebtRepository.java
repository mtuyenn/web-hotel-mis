package com.hospitality.mis.dao.finance;

import com.hospitality.mis.entity.finance.PartnerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PartnerDebtRepository extends JpaRepository<PartnerDebt, Long> {
    Optional<PartnerDebt> findByReferenceCode(String referenceCode);
    List<PartnerDebt> findAllByOrderByRecordedAtDesc();
}
