package com.hospitality.mis.dao.billing;

import com.hospitality.mis.entity.billing.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Kho biên lai phát hành cho hóa đơn. */
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    /** Tra cứu biên lai theo số hiển thị duy nhất. */
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    /** Lấy các biên lai của hóa đơn theo thứ tự phát hành tăng dần. */
    List<Receipt> findByInvoiceIdOrderByIssuedAtAsc(Long invoiceId);
}
