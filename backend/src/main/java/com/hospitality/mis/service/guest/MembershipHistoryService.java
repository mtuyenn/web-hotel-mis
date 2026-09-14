package com.hospitality.mis.service.guest;

import com.hospitality.mis.dao.guest.MembershipHistoryRepository;
import com.hospitality.mis.dto.guest.MembershipHistoryDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Chỉ đọc lịch sử thay đổi hạng thành viên của một khách. */
@Service
public class MembershipHistoryService {
    /** Kho lịch sử, sắp xếp bản ghi mới nhất trước khi trả về. */
    private final MembershipHistoryRepository history;
    public MembershipHistoryService(MembershipHistoryRepository history) { this.history = history; }
    @Transactional(readOnly = true)
    /** Trả các lần chuyển hạng và lý do theo guest id. */
    public java.util.List<MembershipHistoryDtos.Response> list(Long guestId) { return history.findByGuestIdOrderByChangedAtDesc(guestId).stream().map(x -> new MembershipHistoryDtos.Response(x.getId(), x.getGuest().getId(), x.getFromTier(), x.getToTier(), x.getReason(), x.getChangedAt())).toList(); }
}
