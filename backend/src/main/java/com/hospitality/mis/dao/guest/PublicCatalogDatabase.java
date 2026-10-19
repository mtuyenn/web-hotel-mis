package com.hospitality.mis.dao.guest;

import com.hospitality.mis.dto.guest.PublicGuestDtos;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.dao.room.RoomStatusConverter;
import com.hospitality.mis.persistence.VietnameseCodeConverters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/** Allow-listed public views; no guest, reservation or payment entity is loaded. */
@Repository
public class PublicCatalogDatabase {
    private static final String ROOMS="SELECT maPhong,tenPhong,maLoaiPhong,maHangPhong,tenLoaiPhong,tenQuangBa,moTaLoaiPhong,khauHieuQuangBa,moTaQuangBa,moTa,giaTheoNgay,giaTheoGio,dienTich,huongNhin,loaiGiuong,tang,trangThai,soKhachToiDa,duongDanAnhBia FROM dbo.vwPhongCongKhai";
    private final JdbcTemplate jdbc;
    public PublicCatalogDatabase(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public record RoomRow(String id,String name,String typeId,String typeCode,String typeName,String marketingName,
                          String typeDescription,String tagline,String marketingDescription,String description,
                          BigDecimal dailyPrice,BigDecimal hourlyPrice,BigDecimal area,String view,String bedType,
                          Integer floor,RoomStatus status,Integer maxOccupancy,String cover) {}

    public record ListingMedia(Map<String,String> roomImages,Map<String,String> typeImages,
                               Map<String,List<String>> amenities) {
        public String cover(RoomRow room) {
            return roomImages.getOrDefault(room.id(),typeImages.getOrDefault(room.typeId(),room.cover()));
        }
        public List<String> amenitiesFor(RoomRow room) {
            return amenities.getOrDefault(room.typeId(),List.of());
        }
    }

    /** Read public media in batches so catalogue size does not multiply database round trips. */
    public ListingMedia listingMedia() {
        Map<String,String> roomImages=new LinkedHashMap<>(),typeImages=new LinkedHashMap<>();
        Map<String,List<String>> amenities=new LinkedHashMap<>();
        jdbc.query("SELECT maPhong,duongDanTuongDoi FROM dbo.vwAnhPhongCongKhai ORDER BY maPhong,thuTuHienThi,maHinhAnhPhong",rs->{
            roomImages.putIfAbsent(rs.getString(1),publicImageUrl(rs.getString(2)));
        });
        jdbc.query("SELECT maLoaiPhong,duongDanAnh FROM dbo.vwAnhLoaiPhongCongKhai ORDER BY maLoaiPhong,thuTuHienThi,maHinhAnhLoaiPhong",rs->{
            typeImages.putIfAbsent(rs.getString(1),rs.getString(2));
        });
        jdbc.query("SELECT maLoaiPhong,ten FROM dbo.vwTienNghiCongKhai ORDER BY maLoaiPhong,ten,maTienNghi",rs->{
            amenities.computeIfAbsent(rs.getString(1),key->new ArrayList<>()).add(rs.getString(2));
        });
        amenities.replaceAll((key,names)->List.copyOf(names));
        return new ListingMedia(roomImages,typeImages,amenities);
    }

    public List<RoomRow> rooms(String type){
        return jdbc.query(ROOMS+" WHERE (? IS NULL OR maLoaiPhong=?) ORDER BY maLoaiPhong,maPhong",this::roomRow,type,type);
    }
    public Optional<RoomRow> room(String id){
        return jdbc.query(ROOMS+" WHERE maPhong=?",this::roomRow,id).stream().findFirst();
    }
    public List<String> amenities(String type){
        return jdbc.queryForList("SELECT ten FROM dbo.vwTienNghiCongKhai WHERE maLoaiPhong=? ORDER BY ten,maTienNghi",String.class,type);
    }
    public List<String> images(RoomRow room){
        var images=jdbc.queryForList("SELECT duongDanTuongDoi FROM dbo.vwAnhPhongCongKhai WHERE maPhong=? ORDER BY thuTuHienThi,maHinhAnhPhong",String.class,room.id());
        if(!images.isEmpty()) return images.stream().map(PublicCatalogDatabase::publicImageUrl).toList();
        var gallery=jdbc.queryForList("SELECT duongDanAnh FROM dbo.vwAnhLoaiPhongCongKhai WHERE maLoaiPhong=? ORDER BY thuTuHienThi,maHinhAnhLoaiPhong",String.class,room.typeId());
        return !gallery.isEmpty()?gallery:room.cover()==null?List.of():List.of(room.cover());
    }
    private static String publicImageUrl(String path) {
        return path.startsWith("http://")||path.startsWith("https://")?path:"/media/rooms/"+path;
    }
    public List<PublicGuestDtos.ServiceSummary> services(){
        return jdbc.query("SELECT maDichVu,ten,gia,donViTinh,danhMuc,moTa,duongDanAnh,thoiLuongPhut FROM dbo.vwDichVuCongKhai ORDER BY ten",(rs,n)->
                new PublicGuestDtos.ServiceSummary(rs.getString(1),rs.getString(2),rs.getBigDecimal(3),
                        new VietnameseCodeConverters.ServiceUnitConverter().convertToEntityAttribute(rs.getString(4)),
                        new VietnameseCodeConverters.ServiceCategoryConverter().convertToEntityAttribute(rs.getString(5)),rs.getString(6),rs.getString(7),rs.getInt(8)));
    }
    private RoomRow roomRow(ResultSet r,int n)throws SQLException{
        return new RoomRow(r.getString("maPhong"),r.getString("tenPhong"),r.getString("maLoaiPhong"),r.getString("maHangPhong"),
                r.getString("tenLoaiPhong"),r.getString("tenQuangBa"),r.getString("moTaLoaiPhong"),r.getString("khauHieuQuangBa"),
                r.getString("moTaQuangBa"),r.getString("moTa"),r.getBigDecimal("giaTheoNgay"),r.getBigDecimal("giaTheoGio"),
                r.getBigDecimal("dienTich"),r.getString("huongNhin"),r.getString("loaiGiuong"),(Integer)r.getObject("tang"),
                new RoomStatusConverter().convertToEntityAttribute(r.getString("trangThai")),
                (Integer)r.getObject("soKhachToiDa"),r.getString("duongDanAnhBia"));
    }
}
