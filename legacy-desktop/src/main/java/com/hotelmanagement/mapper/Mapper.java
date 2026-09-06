package com.hotelmanagement.mapper;

import com.hotelmanagement.model.dto.*;
import com.hotelmanagement.model.entity.*;
import com.hotelmanagement.model.enums.ChucVuNhanVien;
import com.hotelmanagement.model.enums.TinhTrangBaoTri;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lớp Mapper chịu trách nhiệm chuyển đổi giữa Entity và DTO
 * Giúp tách biệt tầng dữ liệu (Entity) và tầng trình bày (DTO)
 */
public class Mapper {

    // ====================== KHACHHANG ======================
    /**
     * Chuyển từ Entity KhachHang sang DTO
     */
    public static KhachHangDTO toKhachHangDTO(KhachHang kh) {
        if (kh == null)
            return null;

        KhachHangDTO dto = new KhachHangDTO();
        dto.setMaKH(kh.getMaKH());
        dto.setTenKH(kh.getTenKH());
        dto.setCccd(kh.getCccd());
        dto.setSoDienThoai(kh.getSoDienThoai());
        dto.setDiaChi(kh.getDiaChi());
        dto.setEmail(kh.getEmail());
        return dto;
    }

    /**
     * Chuyển từ DTO sang Entity KhachHang
     */
    public static KhachHang toKhachHangEntity(KhachHangDTO dto) {
        if (dto == null)
            return null;

        KhachHang kh = new KhachHang();
        kh.setMaKH(dto.getMaKH());
        kh.setTenKH(dto.getTenKH());
        kh.setCccd(dto.getCccd());
        kh.setSoDienThoai(dto.getSoDienThoai());
        kh.setDiaChi(dto.getDiaChi());
        kh.setEmail(dto.getEmail());
        return kh;
    }

    // ====================== DATPHONG ======================
    /**
     * Chuyển từ Entity DatPhong sang DTO (chi tiết)
     */
    public static DatPhongDTO toDatPhongDTO(DatPhong dp) {
        if (dp == null)
            return null;

        DatPhongDTO dto = new DatPhongDTO();
        dto.setMaDP(dp.getMaDP());
        dto.setNgayDat(dp.getNgayDat());
        dto.setGiaCoc(dp.getGiaCoc());

        // Thông tin khách hàng
        if (dp.getKhachHang() != null) {
            dto.setMaKH(dp.getKhachHang().getMaKH());
            dto.setTenKH(dp.getKhachHang().getTenKH());
            dto.setSoDienThoaiKH(dp.getKhachHang().getSoDienThoai());
        }

        // Thông tin nhân viên
        if (dp.getNhanVien() != null) {
            dto.setMaNV(dp.getNhanVien().getMaNV());
            dto.setTenNV(dp.getNhanVien().getTenNV());
        }

        // Trạng thái từ chi tiết đặt phòng
        if (dp.getChiTietDatPhongs() != null && !dp.getChiTietDatPhongs().isEmpty()) {
            String trangThai = dp.getChiTietDatPhongs().stream()
                    .map(ct -> ct.getTrangThai() != null ? ct.getTrangThai().name() : "")
                    .distinct()
                    .collect(Collectors.joining(", "));
            dto.setTrangThai(trangThai);
        }

        // Chuyển danh sách chi tiết phòng
        if (dp.getChiTietDatPhongs() != null) {
            List<ChiTietDatPhongDTO> chiTietList = dp.getChiTietDatPhongs().stream()
                    .map(Mapper::toChiTietDatPhongDTO)
                    .collect(Collectors.toList());
            dto.setChiTietDatPhongs(chiTietList);
        }

        return dto;
    }

    /**
     * Chuyển từ ChiTietDatPhong Entity sang DTO
     */
    public static ChiTietDatPhongDTO toChiTietDatPhongDTO(ChiTietDatPhong ctp) {
        if (ctp == null)
            return null;

        ChiTietDatPhongDTO dto = new ChiTietDatPhongDTO();
        dto.setMaDP(ctp.getDatPhong() != null ? ctp.getDatPhong().getMaDP() : null);
        dto.setMaPhong(ctp.getPhong() != null ? ctp.getPhong().getMaPhong() : null);
        dto.setNgayNhan(ctp.getNgayNhan());
        dto.setNgayTra(ctp.getNgayTra());
        dto.setTinhTrangPhong(ctp.getTrangThai());
        dto.setSoLanChuyenPhong(ctp.getSoLanChuyenPhong());

        // Thông tin loại phòng và giá
        if (ctp.getPhong() != null && ctp.getPhong().getLoaiPhong() != null) {
            dto.setTenLoaiPhong(ctp.getPhong().getLoaiPhong().getTenLoai());
            dto.setGiaPhong(ctp.getPhong().getLoaiPhong().getGia());
        }

        return dto;
    }

    // ====================== PHONG ======================
    public static PhongDTO toPhongDTO(Phong p) {
        if (p == null)
            return null;

        PhongDTO dto = new PhongDTO();
        dto.setMaPhong(p.getMaPhong());
        dto.setTinhTrang(p.getTinhTrang());
        dto.setMoTa(p.getMoTa());

        if (p.getLoaiPhong() != null) {
            dto.setMaLoaiPhong(p.getLoaiPhong().getMaLoaiPhong());
            dto.setTenLoaiPhong(p.getLoaiPhong().getTenLoai());
            dto.setGia(p.getLoaiPhong().getGia());
        }

        return dto;
    }

    // ====================== HOADON ======================
    public static HoaDonDTO toHoaDonDTO(HoaDon hd) {
        if (hd == null)
            return null;

        HoaDonDTO dto = new HoaDonDTO();
        dto.setMaHoaDon(hd.getMaHoaDon());
        dto.setMaDP(hd.getDatPhong() != null ? hd.getDatPhong().getMaDP() : null);
        dto.setNgayXuatHD(hd.getNgayXuatHD());
        dto.setGiamGia(hd.getGiamGia());

        // Khắc phục lỗi data giả: lấy Tiền cọc từ bảng DatPhong nếu HoaDon lưu thiếu
        java.math.BigDecimal tienCoc = hd.getTienCocDaDong();
        if ((tienCoc == null || tienCoc.compareTo(java.math.BigDecimal.ZERO) == 0) && hd.getDatPhong() != null) {
            tienCoc = hd.getDatPhong().getGiaCoc();
        }
        dto.setTienCocDaDong(tienCoc != null ? tienCoc : java.math.BigDecimal.ZERO);

        dto.setTongTienPhong(hd.getTongTienPhong());
        dto.setTongTienDichVu(hd.getTongTienDichVu());
        dto.setTongTienPhaiTra(hd.getTongTienPhaiTra());
        dto.setPhuongThucThanhToan(hd.getPhuongThucThanhToan());
        dto.setTrangThai(hd.getTrangThai());

        // Thông tin khách hàng
        if (hd.getDatPhong() != null && hd.getDatPhong().getKhachHang() != null) {
            dto.setTenKhachHang(hd.getDatPhong().getKhachHang().getTenKH());
        }

        // Lấy ngày nhận/trả từ chi tiết đầu tiên
        if (hd.getDatPhong() != null && hd.getDatPhong().getChiTietDatPhongs() != null
                && !hd.getDatPhong().getChiTietDatPhongs().isEmpty()) {
            ChiTietDatPhong ctp = hd.getDatPhong().getChiTietDatPhongs().get(0);
            dto.setNgayNhan(ctp.getNgayNhan());
            dto.setNgayTra(ctp.getNgayTra());

            if (ctp.getPhong() != null && ctp.getPhong().getLoaiPhong() != null) {
                dto.setTenLoaiPhong(ctp.getPhong().getLoaiPhong().getTenLoai());
            }
        }

        return dto;
    }

    // ====================== CHUYENPHONG ======================
    public static ChuyenPhongDTO toChuyenPhongDTO(ChuyenPhong cp) {
        if (cp == null)
            return null;

        ChuyenPhongDTO dto = new ChuyenPhongDTO();
        dto.setMaChuyenPhong(cp.getMaChuyenPhong());
        dto.setMaDP(cp.getDatPhong() != null ? cp.getDatPhong().getMaDP() : null);
        dto.setMaPhongCu(cp.getPhongCu() != null ? cp.getPhongCu().getMaPhong() : null);
        dto.setMaPhongMoi(cp.getPhongMoi() != null ? cp.getPhongMoi().getMaPhong() : null);
        dto.setNgayChuyen(cp.getNgayChuyen());
        dto.setLyDo(cp.getLyDo());

        // Thêm tên phòng để hiển thị dễ nhìn
        if (cp.getPhongCu() != null)
            dto.setTenPhongCu(cp.getPhongCu().getMaPhong());
        if (cp.getPhongMoi() != null)
            dto.setTenPhongMoi(cp.getPhongMoi().getMaPhong());

        return dto;
    }

    // ====================== BAOTRI ======================
    public static BaoTriDTO toBaoTriDTO(BaoTri bt) {
        if (bt == null)
            return null;

        BaoTriDTO dto = new BaoTriDTO();
        dto.setMaBT(bt.getMaBT());
        dto.setMaPhong(bt.getPhong() != null ? bt.getPhong().getMaPhong() : null);
        dto.setLoaiBaoTri(bt.getLoaiBaoTri());
        dto.setNgayBaoTri(bt.getNgayBaoTri());
        dto.setMoTa(bt.getMoTa());
        dto.setTinhTrangBTri(bt.getTinhTrangBTri());

        return dto;
    }

    public static BaoTri toBaoTri(BaoTriDTO dto, Phong phong) {
        if (dto == null || phong == null)
            return null;

        BaoTri bt = new BaoTri();
        bt.setMaBT(dto.getMaBT());
        bt.setPhong(phong);
        bt.setLoaiBaoTri(dto.getLoaiBaoTri());
        bt.setNgayBaoTri(dto.getNgayBaoTri());
        bt.setMoTa(dto.getMoTa());
        bt.setTinhTrangBTri(dto.getTinhTrangBTri() != null ? dto.getTinhTrangBTri() : TinhTrangBaoTri.DANG_BAO_TRI);

        return bt;
    }

    // ====================== NHANVIEN ======================
    /**
     * Chuyển từ Entity NhanVien sang NhanVienDTO
     */
    public static NhanVienDTO toNhanVienDTO(NhanVien entity) {
        if (entity == null)
            return null;

        NhanVienDTO dto = new NhanVienDTO();
        dto.setMaNV(entity.getMaNV());
        dto.setTenNV(entity.getTenNV());
        dto.setSoDienThoai(entity.getSoDienThoai());
        dto.setDiaChi(entity.getDiaChi());

        // Chuyển Enum sang String
        if (entity.getChucVu() != null) {
            dto.setChucVu(entity.getChucVu().name());
        } else {
            dto.setChucVu("NHAN_VIEN");
        }

        return dto;
    }

    /**
     * Chuyển từ NhanVienDTO sang Entity NhanVien
     */
    public static NhanVien toNhanVienEntity(NhanVienDTO dto) {
        if (dto == null)
            return null;

        NhanVien nv = new NhanVien();
        nv.setMaNV(dto.getMaNV());
        nv.setTenNV(dto.getTenNV());
        nv.setSoDienThoai(dto.getSoDienThoai());
        nv.setDiaChi(dto.getDiaChi());
        nv.setPassword(dto.getPassword()); // nếu có

        // Xử lý ChucVu - chuyển từ String sang Enum
        if (dto.getChucVu() != null && !dto.getChucVu().isEmpty()) {
            try {
                nv.setChucVu(ChucVuNhanVien.valueOf(dto.getChucVu().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Nếu chuỗi không khớp với enum, mặc định là NHAN_VIEN
                nv.setChucVu(ChucVuNhanVien.NHAN_VIEN);
                System.err.println("Chức vụ không hợp lệ: " + dto.getChucVu() + " → mặc định NHAN_VIEN");
            }
        } else {
            nv.setChucVu(ChucVuNhanVien.NHAN_VIEN);
        }

        return nv;
    }

    // ====================== DICHVU ======================
    public static DichVuDTO toDichVuDTO(DichVu dv) {
        if (dv == null)
            return null;
        DichVuDTO dto = new DichVuDTO();
        dto.setMaDV(dv.getMaDV());
        dto.setTenDichVu(dv.getTenDichVu());
        dto.setGiaDV(dv.getGiaDV());
        return dto;
    }

    public static ChiTietDichVuDTO toChiTietDichVuDTO(ChiTietDichVu ct) {
        if (ct == null)
            return null;
        ChiTietDichVuDTO dto = new ChiTietDichVuDTO(
                ct.getDatPhong() != null ? ct.getDatPhong().getMaDP() : null,
                ct.getDichVu() != null ? ct.getDichVu().getMaDV() : null,
                ct.getDichVu() != null ? ct.getDichVu().getTenDichVu() : null,
                ct.getDichVu() != null ? ct.getDichVu().getGiaDV() : null,
                ct.getNgaySuDung(),
                ct.getSoLuong());
        return dto;
    }
}