package com.hospitality.mis.dao.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dto.operations.TechnicalAssetDtos;
import com.hospitality.mis.persistence.VietnameseCodeConverters;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class TechnicalAssetDatabase {
    private static final VietnameseCodeConverters.TechnicalAssetStatusConverter ASSET_STATUS = new VietnameseCodeConverters.TechnicalAssetStatusConverter();
    private static final VietnameseCodeConverters.TechnicalAssetLocationTypeConverter ASSET_LOCATION_TYPE = new VietnameseCodeConverters.TechnicalAssetLocationTypeConverter();
    private final JdbcTemplate jdbc;

    public TechnicalAssetDatabase(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private <T> List<T> read(String sql, RowMapper<T> mapper, Object... args) {
        return jdbc.query(sql,mapper,args);
    }
    private void command(String sql,Object... args) {
        try { jdbc.update(sql,args); } catch(DataAccessException error) { throw translate(error); }
    }
    public List<TechnicalAssetDtos.TechnicalAssetResponse> assets(){return read("SELECT maTaiSanKyThuat,ten,danhMuc,loaiViTri,maPhong,tang,viTri,thuongHieuMau,ngayLapDat,ngayBaoTriTiepTheo,trangThai,giaTriBanDau,ghiChu,dangHoatDong FROM dbo.vwTaiSanKyThuat WHERE dangHoatDong=1 ORDER BY COALESCE(tang,0),ten",this::assetRow);}
    public void createAsset(TechnicalAssetDtos.TechnicalAssetRequest request,String locationType,String status){command("EXEC dbo.uspTaoTaiSanKyThuat ?,?,?,?,?,?,?,?,?,?,?,?,?",request.id(),request.name(),request.category(),locationType,request.roomId(),request.floor(),request.location(),request.brandModel(),request.installedOn(),request.nextMaintenance(),status,request.originalValue()==null?BigDecimal.ZERO:request.originalValue(),request.note());}
    public void updateAsset(String id,String status){command("EXEC dbo.uspCapNhatTaiSanKyThuat ?,?",id,status);}
    private TechnicalAssetDtos.TechnicalAssetResponse assetRow(java.sql.ResultSet rs, int n) throws java.sql.SQLException { return new TechnicalAssetDtos.TechnicalAssetResponse(rs.getString(1), rs.getString(2), rs.getString(3), ASSET_LOCATION_TYPE.convertToEntityAttribute(rs.getString(4)), rs.getString(5), (Integer) rs.getObject(6), rs.getString(7), rs.getString(8), date(rs, 9), date(rs, 10), ASSET_STATUS.convertToEntityAttribute(rs.getString(11)), rs.getBigDecimal(12), rs.getString(13), rs.getBoolean(14)); }
    private static LocalDate date(java.sql.ResultSet rs, int index) throws java.sql.SQLException { var value = rs.getDate(index); return value == null ? null : value.toLocalDate(); }
    private RuntimeException translate(DataAccessException error) {
        for(Throwable cause=error;cause!=null;cause=cause.getCause()) {
            if(cause instanceof SQLException sql) {
                String code=switch(sql.getErrorCode()) {
                    case 51206 -> "ASSET_NOT_FOUND";
                    default -> null;
                };
                if(code!=null) return new DomainException(code,sql.getMessage());
            }
        }
        return error;
    }
}
