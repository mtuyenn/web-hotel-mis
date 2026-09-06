package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.DichVuService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.model.dao.DichVuDAO;
import com.hotelmanagement.model.dto.DichVuDTO;
import com.hotelmanagement.model.entity.DichVu;
import com.hotelmanagement.mapper.Mapper;

import java.util.List;
import java.util.stream.Collectors;
import com.hotelmanagement.model.dao.impl.DichVuDAOImpl;

public class DichVuServiceImpl implements DichVuService {
    private final DichVuDAO dichVuDAO = new DichVuDAOImpl();

    public List<DichVuDTO> layTatCaDichVu() {
        return dichVuDAO.findAll().stream()
                .map(Mapper::toDichVuDTO)
                .collect(Collectors.toList());
    }

    public DichVuDTO timDichVuTheoMa(String maDV) {
        DichVu entity = dichVuDAO.findByMaDV(maDV);
        return entity != null ? Mapper.toDichVuDTO(entity) : null;
    }

    public void themDichVu(DichVuDTO dto) {
        if (dichVuDAO.findByMaDV(dto.getMaDV()) != null) {
            throw new BusinessException("Mã dịch vụ đã tồn tại trong hệ thống");
        }
        DichVu dv = new DichVu();
        dv.setMaDV(dto.getMaDV());
        dv.setTenDichVu(dto.getTenDichVu());
        dv.setGiaDV(dto.getGiaDV());
        dichVuDAO.save(dv);
    }

    public void capNhatDichVu(DichVuDTO dto) {
        DichVu dv = dichVuDAO.findByMaDV(dto.getMaDV());
        if (dv == null) {
            throw new BusinessException("Không tìm thấy dịch vụ để cập nhật");
        }
        dv.setTenDichVu(dto.getTenDichVu());
        dv.setGiaDV(dto.getGiaDV());
        dichVuDAO.save(dv);
    }

    public void xoaDichVu(String maDV) {
        if (dichVuDAO.findByMaDV(maDV) == null) {
            throw new BusinessException("Không tìm thấy dịch vụ cần xóa");
        }
        dichVuDAO.delete(maDV);
    }

    private final com.hotelmanagement.model.dao.ChiTietDichVuDAO chiTietDichVuDAO = new com.hotelmanagement.model.dao.impl.ChiTietDichVuDAOImpl();
    private final com.hotelmanagement.model.dao.DatPhongDAO datPhongDAO = new com.hotelmanagement.model.dao.impl.DatPhongDAOImpl();

    @Override
    public List<com.hotelmanagement.model.dto.ChiTietDichVuDTO> layDichVuDaDung(Long maDP) {
        return chiTietDichVuDAO.findByMaDP(maDP).stream()
                .map(Mapper::toChiTietDichVuDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void luuDichVuSuDung(Long maDP, List<com.hotelmanagement.model.dto.ChiTietDichVuDTO> selectedServices) {
        com.hotelmanagement.model.entity.DatPhong dp = datPhongDAO.findByMaDP(maDP);
        if (dp == null) throw new BusinessException("Không tìm thấy thông tin đặt phòng");

        // Xóa cũ để cập nhật mới (đơn giản nhất)
        chiTietDichVuDAO.deleteByDatPhong(maDP);

        java.math.BigDecimal tongDV = java.math.BigDecimal.ZERO;
        for (com.hotelmanagement.model.dto.ChiTietDichVuDTO dto : selectedServices) {
            DichVu dv = dichVuDAO.findByMaDV(dto.getMaDV());
            if (dv == null) continue;

            com.hotelmanagement.model.entity.ChiTietDichVu entity = new com.hotelmanagement.model.entity.ChiTietDichVu();
            entity.setDatPhong(dp);
            entity.setDichVu(dv);
            entity.setSoLuong(dto.getSoLuong());
            entity.setNgaySuDung(java.time.LocalDate.now()); // Mặc định ngày hiện tại

            chiTietDichVuDAO.save(entity);
            
            // Tính tổng tiền dịch vụ
            tongDV = tongDV.add(dv.getGiaDV().multiply(java.math.BigDecimal.valueOf(dto.getSoLuong())));
        }

        // Cập nhật lại hóa đơn (nếu có)
        com.hotelmanagement.model.dao.HoaDonDAO hdDAO = new com.hotelmanagement.model.dao.impl.HoaDonDAOImpl();
        com.hotelmanagement.model.entity.HoaDon hd = hdDAO.findByMaDP(maDP);
        if (hd != null) {
            hd.setTongTienDichVu(tongDV);
            
            // Tính toán lại tổng tiền phải trả
            java.math.BigDecimal phong = hd.getTongTienPhong() != null ? hd.getTongTienPhong() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal giam = hd.getGiamGia() != null ? hd.getGiamGia() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal coc = hd.getTienCocDaDong() != null ? hd.getTienCocDaDong() : java.math.BigDecimal.ZERO;
            
            java.math.BigDecimal pay = phong.add(tongDV).subtract(giam).subtract(coc);
            if (pay.compareTo(java.math.BigDecimal.ZERO) < 0) pay = java.math.BigDecimal.ZERO;
            
            hd.setTongTienPhaiTra(pay);
            hdDAO.update(hd);
        }
    }
}
