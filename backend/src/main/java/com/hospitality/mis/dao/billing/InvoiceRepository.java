package com.hospitality.mis.dao.billing;



import com.hospitality.mis.entity.billing.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;



/** Kho hóa đơn, hỗ trợ đọc theo đặt phòng và khóa ghi khi thu tiền.
 * Lưu thông thường vẫn để JPA kiểm tra optimistic version; findForUpdate dùng khóa pessimistic khi cần.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    /** Tìm hóa đơn gắn với một đặt phòng; mỗi đặt phòng chỉ có một hóa đơn hiện hành. */
    Optional<Invoice> findByReservationId(Long reservationId);

    /** Tải hóa đơn dưới khóa ghi để cập nhật số dư hoặc trạng thái mà không tranh chấp đồng thời. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findForUpdate(@Param("id") Long id);
}
