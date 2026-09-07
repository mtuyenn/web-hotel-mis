package com.hospitality.mis.billing.domain;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
public class ChiTietDichVuId implements Serializable {
    private Long reservation; private String service; private LocalDate usedOn;
    public ChiTietDichVuId() {}
    public ChiTietDichVuId(Long reservation,String service,LocalDate usedOn){this.reservation=reservation;this.service=service;this.usedOn=usedOn;}
    public Long getReservation(){return reservation;} public void setReservation(Long v){reservation=v;}
    public String getService(){return service;} public void setService(String v){service=v;}
    public LocalDate getUsedOn(){return usedOn;} public void setUsedOn(LocalDate v){usedOn=v;}
    public boolean equals(Object o){if(this==o)return true;if(!(o instanceof ChiTietDichVuId x))return false;return Objects.equals(reservation,x.reservation)&&Objects.equals(service,x.service)&&Objects.equals(usedOn,x.usedOn);}
    public int hashCode(){return Objects.hash(reservation,service,usedOn);}
}
