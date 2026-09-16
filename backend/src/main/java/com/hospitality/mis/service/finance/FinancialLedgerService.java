package com.hospitality.mis.service.finance;

import com.hospitality.mis.dao.finance.FinancialLedgerEntryRepository;
import com.hospitality.mis.entity.finance.FinancialLedgerEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

/** Ghi bút toán finalized bất biến cho mọi biến động tài chính đã xác nhận. */
@Service
public class FinancialLedgerService {
    private final FinancialLedgerEntryRepository entries;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    public FinancialLedgerService(FinancialLedgerEntryRepository entries) { this.entries = entries; }
    @org.springframework.beans.factory.annotation.Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String type, String sourceType, String sourceId, FinancialLedgerEntry.Direction direction,
                       BigDecimal amount, String actor, LocalDateTime occurredAt, String note) {
        if (amount == null || amount.signum() <= 0) return;
        var entry = new FinancialLedgerEntry(); entry.setEntryType(type); entry.setSourceType(sourceType);
        entry.setSourceId(sourceId); entry.setDirection(direction); entry.setAmount(amount);
        entry.setActorId(actor == null || actor.isBlank() ? "SYSTEM" : actor);
        entry.setOccurredAt(occurredAt == null ? LocalDateTime.now(clock) : occurredAt); entry.setNote(note);
        entries.save(entry);
    }
}
