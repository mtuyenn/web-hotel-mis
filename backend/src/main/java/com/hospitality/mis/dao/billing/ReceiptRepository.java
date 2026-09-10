package com.hospitality.mis.dao.billing;

import com.hospitality.mis.entity.billing.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
    List<Receipt> findByInvoiceIdOrderByIssuedAtAsc(Long invoiceId);
}
