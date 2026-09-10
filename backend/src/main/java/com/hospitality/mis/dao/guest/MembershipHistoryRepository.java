package com.hospitality.mis.dao.guest;

import com.hospitality.mis.entity.guest.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Long> {
    List<MembershipHistory> findByGuestIdOrderByChangedAtDesc(Long guestId);
}
