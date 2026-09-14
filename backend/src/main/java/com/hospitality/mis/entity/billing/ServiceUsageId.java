/* Khóa ghép xác định duy nhất một dịch vụ cho một reservation. */
package com.hospitality.mis.entity.billing;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/** Giá trị định danh cho khóa chính ghép của service_usages. */
public class ServiceUsageId implements Serializable {
    /** ID đặt phòng trong khóa ghép, ánh xạ từ quan hệ @Id của entity. */
    private Long reservation;
    /** Mã dịch vụ trong khóa ghép. */
    private String service;
    /** Ngày sử dụng trong khóa ghép. */
    private LocalDate usedOn;

    /** Constructor rỗng bắt buộc cho IdClass/JPA. */
    public ServiceUsageId() {}
    /** Tạo định danh đầy đủ cho một dòng sử dụng dịch vụ. */
    public ServiceUsageId(Long reservation, String service, LocalDate usedOn) {
        this.reservation = reservation; this.service = service; this.usedOn = usedOn;
    }
    public Long getReservation() { return reservation; }
    public void setReservation(Long value) { reservation = value; }
    public String getService() { return service; }
    public void setService(String value) { service = value; }
    public LocalDate getUsedOn() { return usedOn; }
    public void setUsedOn(LocalDate value) { usedOn = value; }
    /** So sánh đủ ba thành phần vì thiếu một thành phần có thể trộn hai dòng dịch vụ. */
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ServiceUsageId value)) return false;
        return Objects.equals(reservation, value.reservation)
                && Objects.equals(service, value.service)
                && Objects.equals(usedOn, value.usedOn);
    }
    /** Hash nhất quán với khóa ghép để dùng an toàn trong collection/JPA. */
    @Override public int hashCode() { return Objects.hash(reservation, service, usedOn); }
}
