package com.hospitality.mis.billing.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name = "services") @Access(AccessType.FIELD)
public class Service {
    @Id @Column(name="id", length=10) private String id;
    @Column(name="name", nullable=false, length=100) private String name;
    @Column(name="price", nullable=false, precision=10, scale=2) private BigDecimal price;
    @Column(name="unit", nullable=false, length=20) private String unit="LẦN";
    @Column(name="stock_quantity", nullable=false) private int stockQuantity;
    @Column(name="safety_threshold", nullable=false) private int safetyThreshold;
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
    public String getUnit(){return unit;} public void setUnit(String v){unit=v;}
    public int getStockQuantity(){return stockQuantity;} public void setStockQuantity(int v){stockQuantity=v;}
    public int getSafetyThreshold(){return safetyThreshold;} public void setSafetyThreshold(int v){safetyThreshold=v;}
}
