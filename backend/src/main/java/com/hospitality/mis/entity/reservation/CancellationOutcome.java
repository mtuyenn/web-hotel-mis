package com.hospitality.mis.entity.reservation;

/** Quyết định quyết toán rõ ràng do chính sách hủy đặt phòng đưa ra. */
public enum CancellationOutcome {
    /** Hoàn lại tiền theo chính sách hủy. */
    REFUND,
    /** Giữ lại tiền cọc hoặc khoản đã thu. */
    RETAIN,
    /** Mất toàn bộ khoản đủ điều kiện bị khấu trừ. */
    FORFEIT
}
