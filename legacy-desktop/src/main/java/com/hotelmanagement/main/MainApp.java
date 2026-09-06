package com.hotelmanagement.main;

import com.hotelmanagement.view.LoginForm;
import javax.swing.*;

/**
 * MAIN APP - Điểm khởi chạy chính của toàn bộ dự án Quản lý Khách sạn
 */
public class MainApp {

    public static void main(String[] args) {

        // Thiết lập giao diện đẹp hơn (Look and Feel)
        try {
            // Sử dụng FlatLaf cho giao diện phẳng hiện đại
            com.formdev.flatlaf.FlatLightLaf.setup();
            // Đặt font chữ chung cho mượt
            UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        } catch (Exception e) {
            System.out.println("Không thể thiết lập Look and Feel: " + e.getMessage());
        }

        // Chạy giao diện trên EDT (Event Dispatch Thread) - Bắt buộc với Swing
        SwingUtilities.invokeLater(() -> {

            // Hiển thị form đăng nhập trước
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}