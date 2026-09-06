package com.hotelmanagement.view.phong;

import com.hotelmanagement.controller.ChuyenPhongController;
import com.hotelmanagement.controller.PhongController;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.enums.TinhTrangPhong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Lớp ChuyenPhongDialog là giao diện (UI) dùng để thực hiện
 * chức năng chuyển phòng cho khách hàng trong quá trình lưu trú.
 * 
 * Lớp này cho phép:
 * - Chọn phòng mới đang sẵn sàng
 * - Nhập lý do chuyển phòng
 * - Xác nhận và thực hiện chuyển phòng thông qua Controller
 */
public class ChuyenPhongDialog extends JDialog {
    private final Long maDP;
    private final PhongDTO phongCu;
    private final Runnable onSuccess;

    private JComboBox<RoomComboItem> cbPhongMoi;
    private JTextField txtLyDo;

    // Lớp phụ dùng để hiển thị thông tin phòng trong ComboBox
    // Giúp hiển thị đầy đủ: mã phòng + loại phòng + giá
    private static class RoomComboItem {
        PhongDTO phong;

        // Lớp phụ dùng để hiển thị thông tin phòng trong ComboBox
        // Giúp hiển thị đầy đủ: mã phòng + loại phòng + giá
        public RoomComboItem(PhongDTO phong) {
            this.phong = phong;
        }

        @Override
        public String toString() {
            String tenLoai = phong.getTenLoaiPhong() != null ? phong.getTenLoaiPhong() : "Chưa rõ";
            return phong.getMaPhong() + " - " + tenLoai + " (" + String.format("%,.0f", phong.getGia()) + "đ)";
        }
    }

    public ChuyenPhongDialog(JDialog parent, Long maDP, PhongDTO phongCu, Runnable onSuccess) {
        super(parent, "Chuyển phòng (" + phongCu.getMaPhong() + ")", true);
        this.maDP = maDP;
        this.phongCu = phongCu;
        this.onSuccess = onSuccess;
        initUI();
    }

    // Khởi tạo giao diện gồm:
    // - ComboBox chọn phòng mới (lọc phòng có trạng thái SẴN SÀNG)
    // - TextField nhập lý do chuyển phòng
    // - Nút xác nhận và hủy
    private void initUI() {
        setSize(450, 280);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.weightx = 1.0;

        // 1. Label Phòng mới
        JLabel lblRoom = new JLabel("Chọn phòng mới:");
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRoom.setForeground(new Color(60, 60, 60));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblRoom, gbc);

        // 2. ComboBox Phòng mới
        cbPhongMoi = new JComboBox<>();
        cbPhongMoi.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbPhongMoi.setBackground(Color.WHITE);
        cbPhongMoi.setPreferredSize(new Dimension(300, 38));

        // Đổ dữ liệu các phòng SẴN SÀNG
        try {
            List<PhongDTO> tatCaPhong = new PhongController().getAllPhong();
            for (PhongDTO p : tatCaPhong) {
                if (p.getTinhTrang() == TinhTrangPhong.SAN_SANG) {
                    cbPhongMoi.addItem(new RoomComboItem(p));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (cbPhongMoi.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hiện tại không còn phòng trống nào để chuyển!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
        }

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(cbPhongMoi, gbc);

        // 3. Label Lý do
        JLabel lblLyDo = new JLabel("Lý do chuyển phòng:");
        lblLyDo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLyDo.setForeground(new Color(60, 60, 60));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(lblLyDo, gbc);

        // 4. Text Field Lý do
        txtLyDo = new JTextField();
        txtLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtLyDo.setPreferredSize(new Dimension(300, 38));
        txtLyDo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(txtLyDo, gbc);

        add(panel, BorderLayout.CENTER);

        // --- Nút Xác nhận Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setForeground(new Color(100, 100, 100));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        JButton btnOk = new JButton("Chuyển phòng");
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnOk.setBackground(new Color(46, 204, 113));
        btnOk.setForeground(Color.WHITE);
        btnOk.setPreferredSize(new Dimension(140, 40));
        btnOk.setFocusPainted(false);
        btnOk.setBorderPainted(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnOk.addActionListener(e -> chotChuyenPhong());

        footer.add(btnCancel);
        footer.add(btnOk);
        add(footer, BorderLayout.SOUTH);
    }

    // Xử lý logic khi xác nhận chuyển phòng:
    // 1. Kiểm tra đã chọn phòng mới và nhập lý do
    // 2. Hiển thị hộp thoại xác nhận
    // 3. Gọi Controller để thực hiện chuyển phòng
    // 4. Thông báo thành công và đóng dialog
    private void chotChuyenPhong() {
        if (cbPhongMoi.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng mới!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String lyDo = txtLyDo.getText();
        if (lyDo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do chuyển phòng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        RoomComboItem selectedItem = (RoomComboItem) cbPhongMoi.getSelectedItem();
        String maPhongMoi = selectedItem.phong.getMaPhong();

        try {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Bạn muốn chuyển từ phòng " + phongCu.getMaPhong() + " sang phòng " + maPhongMoi + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (opt == JOptionPane.YES_OPTION) {
                new ChuyenPhongController().chuyenPhong(maDP, phongCu.getMaPhong(), maPhongMoi, lyDo);
                JOptionPane.showMessageDialog(this, "Chuyển phòng hoàn tất!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                if (onSuccess != null) {
                    onSuccess.run();
                }
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi chuyển phòng: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
