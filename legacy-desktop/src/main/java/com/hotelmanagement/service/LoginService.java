package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.NhanVienDTO;

/**
 * Interface for LoginService
 */
public interface LoginService {
    public NhanVienDTO login(String tenNV, String password);
}
