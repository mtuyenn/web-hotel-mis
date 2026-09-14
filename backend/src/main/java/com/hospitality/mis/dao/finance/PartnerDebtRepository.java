package com.hospitality.mis.dao.finance;

import com.hospitality.mis.entity.finance.PartnerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

/** Kho công nợ đối tác và mã tham chiếu nghiệp vụ. */
public interface PartnerDebtRepository extends JpaRepository<PartnerDebt, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from PartnerDebt d where d.id = :id")
    Optional<PartnerDebt> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    /** Tra cứu công nợ đối tác bằng mã tham chiếu nghiệp vụ. */
    Optional<PartnerDebt> findByReferenceCode(String referenceCode);

    /** Lấy sổ công nợ theo thời điểm ghi nhận mới nhất trước. */
    List<PartnerDebt> findAllByOrderByRecordedAtDesc();
}
