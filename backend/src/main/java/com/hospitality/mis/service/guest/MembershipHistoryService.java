package com.hospitality.mis.service.guest;

import com.hospitality.mis.dao.guest.MembershipHistoryRepository;
import com.hospitality.mis.dto.guest.MembershipHistoryDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipHistoryService {
    private final MembershipHistoryRepository history;
    public MembershipHistoryService(MembershipHistoryRepository history) { this.history = history; }
    @Transactional(readOnly = true)
    public java.util.List<MembershipHistoryDtos.Response> list(Long guestId) { return history.findByGuestIdOrderByChangedAtDesc(guestId).stream().map(x -> new MembershipHistoryDtos.Response(x.getId(), x.getGuest().getId(), x.getFromTier(), x.getToTier(), x.getReason(), x.getChangedAt())).toList(); }
}
