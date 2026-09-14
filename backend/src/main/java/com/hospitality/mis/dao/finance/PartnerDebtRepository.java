package com.hospitality.mis.dao.finance;

import com.hospitality.mis.entity.finance.PartnerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/** Kho công nợ đối tác và mã tham chiếu nghiệp vụ. */
public interface PartnerDebtRepository extends JpaRepository<PartnerDebt, Long> {
    /** Tra cứu công nợ đối tác bằng mã tham chiếu nghiệp vụ. */
    Optional<PartnerDebt> findByReferenceCode(String referenceCode);

    /** Lấy sổ công nợ theo thời điểm ghi nhận mới nhất trước. */
    List<PartnerDebt> findAllByOrderByRecordedAtDesc();
}
