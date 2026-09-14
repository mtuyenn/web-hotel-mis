/* Entity này lưu danh mục dịch vụ và đơn vị tính được dùng trong hóa đơn. */
package com.hospitality.mis.entity.billing;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name = "services") @Access(AccessType.FIELD)
/** Danh mục dịch vụ và tồn kho dùng để lập dòng dịch vụ trên hóa đơn. */
public class Service {
    /** Mã dịch vụ nghiệp vụ, được tham chiếu bởi các dòng sử dụng dịch vụ. */
    @Id @Column(name="id", length=10) private String id;
    /** Tên hiển thị của dịch vụ. */
    @Column(name="name", nullable=false, length=100) private String name;
    /** Đơn giá hiện tại của danh mục; giá lịch sử được chụp ở ServiceUsage. */
    @Column(name="price", nullable=false, precision=10, scale=2) private BigDecimal price;
    /** Đơn vị tính, mặc định một lần sử dụng. */
    @Column(name="unit", nullable=false, length=20) private String unit="TIME";
    /** Số lượng tồn kho hiện tại. */
    @Column(name="stock_quantity", nullable=false) private int stockQuantity;
    /** Ngưỡng cảnh báo khi tồn kho xuống thấp. */
    @Column(name="safety_threshold", nullable=false) private int safetyThreshold;
    /** Chỉ dịch vụ active mới được đưa vào public catalog. */
    @Column(name="active", nullable=false) private boolean active = true;
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
    public String getUnit(){return unit;} public void setUnit(String v){unit=v;}
    public int getStockQuantity(){return stockQuantity;} public void setStockQuantity(int v){stockQuantity=v;}
    public int getSafetyThreshold(){return safetyThreshold;} public void setSafetyThreshold(int v){safetyThreshold=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
