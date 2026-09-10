package com.hospitality.mis.entity.billing;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/** Identifier value for the service_usages composite primary key. */
public class ServiceUsageId implements Serializable {
    private Long reservation;
    private String service;
    private LocalDate usedOn;

    public ServiceUsageId() {}
    public ServiceUsageId(Long reservation, String service, LocalDate usedOn) {
        this.reservation = reservation; this.service = service; this.usedOn = usedOn;
    }
    public Long getReservation() { return reservation; }
    public void setReservation(Long value) { reservation = value; }
    public String getService() { return service; }
    public void setService(String value) { service = value; }
    public LocalDate getUsedOn() { return usedOn; }
    public void setUsedOn(LocalDate value) { usedOn = value; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ServiceUsageId value)) return false;
        return Objects.equals(reservation, value.reservation)
                && Objects.equals(service, value.service)
                && Objects.equals(usedOn, value.usedOn);
    }
    @Override public int hashCode() { return Objects.hash(reservation, service, usedOn); }
}
