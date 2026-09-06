package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.KhachHangDTO;
import java.util.List;

/**
 * Interface for KhachHangService
 */
public interface KhachHangService {
    public void themKhachHang(KhachHangDTO dto);

    public boolean kiemTraTrungCCCD(String cccd);

    public KhachHangDTO timKhachHangByCccd(String cccd);

    public List<KhachHangDTO> layTatCaKhachHang();

    public void capNhatKhachHang(KhachHangDTO dto);

    public void xoaKhachHang(String cccd);
}
