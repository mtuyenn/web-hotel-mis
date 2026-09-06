package com.hotelmanagement.controller;

import com.hotelmanagement.model.dto.NhanVienDTO;
import com.hotelmanagement.service.LoginService;
import com.hotelmanagement.service.impl.LoginServiceImpl;

/**
 * Lớp LoginController dùng để xử lý chức năng đăng nhập
 * của nhân viên trong hệ thống.
 * 
 * Lớp này tiếp nhận thông tin đăng nhập từ giao diện
 * và gọi Service để kiểm tra tài khoản và mật khẩu.
 */
public class LoginController {

    // Khởi tạo service xử lý đăng nhập
    private final LoginService loginService = new LoginServiceImpl();

    /**
     * Thực hiện đăng nhập
     * 
     * @param tenNV    tên đăng nhập của nhân viên
     * @param password mật khẩu
     * @return thông tin nhân viên nếu đăng nhập thành công
     */
    public NhanVienDTO login(String tenNV, String password) {
        return loginService.login(tenNV, password);
    }
}