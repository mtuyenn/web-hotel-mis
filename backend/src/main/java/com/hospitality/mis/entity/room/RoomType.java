package com.hospitality.mis.entity.room;



import jakarta.persistence.Access;

import jakarta.persistence.AccessType;

import jakarta.persistence.Column;

import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import java.util.LinkedHashSet;
import java.util.Set;


import java.math.BigDecimal;



/**

 * Trạng thái và ánh xạ chuẩn của loại phòng.

 *

 * Chủ thể JPA cụ thể duy nhất của bảng {@code room_types}.
 */
@Entity
@Table(name = "room_types")
@Access(AccessType.FIELD)
public class RoomType {
    /** Mã loại phòng dùng làm khóa quan hệ từ Room. */
    @Id

    @Column(name = "id", length = 10, nullable = false)
    private String id;



    @Column(name = "name", nullable = false, length = 50)
    /** Tên hiển thị của loại phòng. */
    private String name;



    @Column(name = "daily_price", nullable = false, precision = 12, scale = 2)
    /** Giá cơ bản mỗi ngày, dùng khi tính tiền phòng. */
    private BigDecimal dailyPrice;



    @Column(name = "description", length = 500)
    /** Mô tả tiện nghi hoặc quy định của loại phòng. */
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "room_type_amenities",
            joinColumns = @JoinColumn(name = "room_type_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id"))
    @OrderBy("name ASC")
    private Set<Amenity> amenities = new LinkedHashSet<>();



    /** Constructor rỗng dành cho JPA. */
    public RoomType() {
    }



    public String getId() {

        return id;

    }



    public void setId(String id) {

        this.id = id;

    }





    public String getName() {

        return name;

    }



    public void setName(String name) {

        this.name = name;

    }



    public BigDecimal getDailyPrice() {

        return dailyPrice;

    }



    public void setDailyPrice(BigDecimal dailyPrice) {

        this.dailyPrice = dailyPrice;

    }



    public String getDescription() {

        return description;

    }



    public void setDescription(String description) {

        this.description = description;

    }

    public Set<Amenity> getAmenities() { return amenities; }

}
