package com.hospitality.mis.dao.finance;

import com.hospitality.mis.entity.finance.CashShiftHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Kho các biên bản bàn giao tiền mặt giữa các ca. */
public interface CashShiftHandoverRepository extends JpaRepository<CashShiftHandover, Long> {
    /** Lấy các lần bàn giao của một ca, lần gần nhất đứng trước. */
    List<CashShiftHandover> findByShiftCodeOrderByHandedOverAtDesc(String shiftCode);

    /** Lấy toàn bộ lịch sử bàn giao theo thời điểm giảm dần cho màn hình giám sát. */
    List<CashShiftHandover> findAllByOrderByHandedOverAtDesc();

    /** Lấy lần bàn giao gần nhất do một nhân viên bàn giao thực hiện. */
    Optional<CashShiftHandover> findFirstByFromActorOrderByHandedOverAtDesc(String fromActor);
}
