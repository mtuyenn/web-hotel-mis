package com.hospitality.mis.dao.finance;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dto.finance.FinanceBackofficeDtos;
import com.hospitality.mis.persistence.VietnameseCodeConverters;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FinanceBackofficeDatabase {
    private static final VietnameseCodeConverters.OtaReconciliationStatusConverter OTA_STATUS = new VietnameseCodeConverters.OtaReconciliationStatusConverter();
    private static final VietnameseCodeConverters.VatCustomerTypeConverter VAT_CUSTOMER_TYPE = new VietnameseCodeConverters.VatCustomerTypeConverter();
    private static final VietnameseCodeConverters.VatInvoiceStatusConverter VAT_STATUS = new VietnameseCodeConverters.VatInvoiceStatusConverter();
    private static final VietnameseCodeConverters.VatXmlStatusConverter VAT_XML_STATUS = new VietnameseCodeConverters.VatXmlStatusConverter();
    private final JdbcTemplate jdbc;

    public FinanceBackofficeDatabase(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private <T> List<T> read(String sql, RowMapper<T> mapper, Object... args) {
        return jdbc.query(sql,mapper,args);
    }
    private void command(String sql,Object... args) {
        try { jdbc.update(sql,args); } catch(DataAccessException error) { throw translate(error); }
    }
    private long create(String sql,Object... args) {
        try { return jdbc.queryForObject(sql,Long.class,args); } catch(DataAccessException error) { throw translate(error); }
    }
    private static final String VAT="SELECT maHoaDonGiaTriGiaTang,maHoaDon,soHoaDonGiaTriGiaTang,thueSuat,soTienChiuThue,loaiKhachHang,tenKhachHang,maSoThue,tenCongTy,diaChiCongTy,trangThai,trangThaiXml,thoiDiemPhatHanh,nguoiTao FROM dbo.vwHoaDonGiaTriGiaTang";
    public List<FinanceBackofficeDtos.OtaReconciliationResponse> ota() {
        return read("SELECT maPhieuDatPhong,hoVaTen,maPhong,nguonDatPhong,doanhThuGop,hoaHongOta,doanhThuRong,trangThaiDoiSoatOta,thoiDiemDat FROM dbo.vwDoiSoatOta ORDER BY thoiDiemDat DESC",this::otaRow);
    }
    public void updateOta(long id,String status){command("EXEC dbo.uspCapNhatDoiSoatOta ?,?",id,status);}
    public List<FinanceBackofficeDtos.VatInvoiceResponse> vatInvoices(){return read(VAT+" ORDER BY thoiDiemPhatHanh DESC",(rs,n)->vat(rs));}
    public List<FinanceBackofficeDtos.VatInvoiceResponse> vatInvoice(long id){return read(VAT+" WHERE maHoaDonGiaTriGiaTang=?",(rs,n)->vat(rs),id);}
    public long createVat(FinanceBackofficeDtos.VatInvoiceRequest request,String number,BigDecimal rate,String customerType,String actor){
        return create("EXEC dbo.uspPhatHanhHoaDonVat ?,?,?,?,?,?,?,?,?,?",request.invoiceId(),number,rate,request.taxableAmount(),customerType,request.customerName().trim(),request.taxCode(),request.companyName(),request.companyAddress(),actor);
    }
    public void saveVatXml(long id,String xml){command("EXEC dbo.uspLuuXmlHoaDonVat ?,?",id,xml);}
    private FinanceBackofficeDtos.OtaReconciliationResponse otaRow(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        return new FinanceBackofficeDtos.OtaReconciliationResponse(rs.getLong(1), rs.getString(2), rs.getString(3),
                new VietnameseCodeConverters.BookingSourceConverter().convertToEntityAttribute(rs.getString(4)),
                rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7),
                OTA_STATUS.convertToEntityAttribute(rs.getString(8)), rs.getTimestamp(9).toLocalDateTime());
    }
    private FinanceBackofficeDtos.VatInvoiceResponse vat(java.sql.ResultSet rs) throws java.sql.SQLException {
        BigDecimal rate = rs.getBigDecimal(4);
        BigDecimal taxable = rs.getBigDecimal(5);
        BigDecimal tax = taxable.multiply(rate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        return new FinanceBackofficeDtos.VatInvoiceResponse(rs.getLong(1), rs.getLong(2), rs.getString(3), rate, taxable, tax,
                taxable.add(tax), VAT_CUSTOMER_TYPE.convertToEntityAttribute(rs.getString(6)), rs.getString(7),
                rs.getString(8), rs.getString(9), rs.getString(10), VAT_STATUS.convertToEntityAttribute(rs.getString(11)),
                VAT_XML_STATUS.convertToEntityAttribute(rs.getString(12)), rs.getTimestamp(13).toLocalDateTime(), rs.getString(14));
    }
    private RuntimeException translate(DataAccessException error) {
        for(Throwable cause=error;cause!=null;cause=cause.getCause()) {
            if(cause instanceof SQLException sql) {
                String code=switch(sql.getErrorCode()) {
                    case 51201 -> "OTA_RESERVATION_NOT_FOUND";
                    case 51202 -> "VAT_EXISTS";
                    case 51203 -> "VAT_NOT_FOUND";
                    case 51207 -> "INVOICE_NOT_FOUND";
                    case 51211 -> "VAT_INVOICE_NOT_FINALIZED";
                    case 51212 -> "VAT_AMOUNT_MISMATCH";
                    default -> null;
                };
                if(code!=null) return new DomainException(code,sql.getMessage());
            }
        }
        return error;
    }
}
