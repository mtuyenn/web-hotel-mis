package com.hotelmanagement.view.hoadon;

import com.hotelmanagement.model.dto.HoaDonDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Lớp ChiTietHoaDonDialog - Dialog hiển thị chi tiết hóa đơn
 * 
 * Chức năng:
 * - Hiển thị thông tin chi tiết của hóa đơn: mã hóa đơn, mã đặt phòng, khách
 * hàng, ngày xuất, tiền phòng, tiền dịch vụ, giảm giá, tiền cọc, tổng tiền,
 * trạng thái, phương thức thanh toán
 * - Định dạng tiền tệ theo chuẩn Việt Nam (VND)
 * - Hiển thị ngày giờ theo định dạng dd/MM/yyyy HH:mm
 * - Cho phép đóng dialog để quay lại màn hình danh sách hóa đơn
 * 
 * Thiết kế:
 * - Giao diện dạng Dialog với header, nội dung và footer
 * - Sử dụng GridBagLayout để sắp xếp các thông tin chi tiết
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class ChiTietHoaDonDialog extends JDialog {

    public ChiTietHoaDonDialog(Frame owner, HoaDonDTO dto) {
        super(owner, "CHI TIẾT HÓA ĐƠN #" + dto.getMaHoaDon(), true);

        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("HÓA ĐƠN THANH TOÁN");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(new Color(212, 175, 55)); // Gold
        headerPanel.add(lblHeader);

        // Center Panel with Bill Info
        JPanel billPanel = new JPanel(new GridBagLayout());
        billPanel.setBackground(Color.WHITE);
        billPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 15);

        int y = 0;
        addDetailRow(billPanel, gbc, "Mã Hóa Đơn:", String.valueOf(dto.getMaHoaDon()), y++, labelFont, valueFont);
        addDetailRow(billPanel, gbc, "Mã Đặt Phòng:", dto.getMaDP() != null ? String.valueOf(dto.getMaDP()) : "N/A",
                y++, labelFont, valueFont);
        addDetailRow(billPanel, gbc, "Khách Hàng:",
                dto.getTenKhachHang() != null ? dto.getTenKhachHang() : "Khách Vãng Lai", y++, labelFont, valueFont);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String ngayXuat = dto.getNgayXuatHD() != null ? dto.getNgayXuatHD().format(dtf) : "N/A";
        addDetailRow(billPanel, gbc, "Ngày Xuất HD:", ngayXuat, y++, labelFont, valueFont);

        // Separator
        gbc.gridx = 0;
        gbc.gridy = y++;
        gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);
        billPanel.add(sep, gbc);
        gbc.gridwidth = 1;

        // Money formatting
        DecimalFormat formatter = new DecimalFormat("#,### VND");
        String tienPhong = dto.getTongTienPhong() != null ? formatter.format(dto.getTongTienPhong()) : "0 VND";
        String tienDV = dto.getTongTienDichVu() != null ? formatter.format(dto.getTongTienDichVu()) : "0 VND";
        String giamGia = dto.getGiamGia() != null ? formatter.format(dto.getGiamGia()) : "0 VND";
        String coc = dto.getTienCocDaDong() != null ? formatter.format(dto.getTienCocDaDong()) : "0 VND";
        String tongTien = dto.getTongTienPhaiTra() != null ? formatter.format(dto.getTongTienPhaiTra()) : "0 VND";

        addDetailRow(billPanel, gbc, "Tiền Phòng:", tienPhong, y++, labelFont, valueFont);
        addDetailRow(billPanel, gbc, "Tiền Dịch Vụ:", tienDV, y++, labelFont, valueFont);
        addDetailRow(billPanel, gbc, "Giảm Giá:", giamGia, y++, labelFont, valueFont);
        addDetailRow(billPanel, gbc, "Đã Cọc Trước:", coc, y++, labelFont, valueFont);

        // Separator
        gbc.gridx = 0;
        gbc.gridy = y++;
        gbc.gridwidth = 2;
        billPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Total
        JLabel lblTotalLabel = new JLabel("CẦN THANH TOÁN:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalLabel.setForeground(new Color(231, 76, 60)); // Red
        gbc.gridx = 0;
        gbc.gridy = y;
        billPanel.add(lblTotalLabel, gbc);

        JLabel lblTotalValue = new JLabel(tongTien);
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalValue.setForeground(new Color(231, 76, 60));
        lblTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 1;
        gbc.gridy = y++;
        billPanel.add(lblTotalValue, gbc);

        // Status
        gbc.insets = new Insets(15, 5, 8, 5); // extra top margin
        addDetailRow(billPanel, gbc, "Trạng Thái:", dto.getTrangThai() != null ? dto.getTrangThai().name() : "N/A", y++,
                labelFont, valueFont);
        gbc.insets = new Insets(8, 5, 8, 5);
        addDetailRow(billPanel, gbc, "Phương Thức:",
                dto.getPhuongThucThanhToan() != null ? dto.getPhuongThucThanhToan().name() : "N/A", y++, labelFont,
                valueFont);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        JButton btnClose = new JButton("ĐÓNG");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(140, 42));
        btnClose.setFocusPainted(false);
        btnClose.setBackground(new Color(226, 232, 240));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(billPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int y, Font lFont,
            Font vFont) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(lFont);
        lbl.setForeground(new Color(100, 116, 139)); // Slate-500
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1.0;
        JLabel val = new JLabel(value);
        val.setFont(vFont);
        val.setForeground(Color.BLACK);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(val, gbc);
    }
}