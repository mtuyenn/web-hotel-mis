package com.hotelmanagement.view.phong;

import com.hotelmanagement.model.dto.BaoTriDTO;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.controller.BaoTriController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp CreateBaoTriDialog là giao diện (UI) dùng để tạo mới
 * một yêu cầu bảo trì cho phòng trong hệ thống.
 * 
 * Lớp này cho phép:
 * - Chọn loại bảo trì từ danh sách có sẵn
 * - Thêm loại bảo trì mới
 * - Nhập mô tả chi tiết
 * - Thực hiện tạo yêu cầu bảo trì thông qua Controller
 */
public class CreateBaoTriDialog extends JDialog {

    private final PhongDTO phong;
    private final Runnable onSuccess;
    private JComboBox<String> cbLoaiBaoTri;
    private JTextField txtMoTa;

    public CreateBaoTriDialog(JDialog parent, PhongDTO phong, Runnable onSuccess) {
        super(parent, "Chuyển sang trạng thái Bảo trì", true);
        this.phong = phong;
        this.onSuccess = onSuccess;
        initUI();
    }

    // Khởi tạo giao diện gồm:
    // - ComboBox chọn loại bảo trì (lấy từ database)
    // - Nút thêm loại bảo trì mới
    // - TextField nhập mô tả
    // - Nút xác nhận và hủy
    private void initUI() {
        setSize(420, 280);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- Panel Nội dung chính dùng GridBagLayout cho chuẩn ---
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);

        // 1. Label Loại bảo trì
        JLabel lblType = new JLabel("Loại bảo trì:");
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblType.setForeground(new Color(60, 60, 60));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblType, gbc);

        // 2. ComboBox + Nút Add
        // Lấy danh sách các loại bảo trì đang tồn tại thật sự dưới Database
        List<String> typesList = new ArrayList<>();

        try {
            List<String> dbTypes = new BaoTriController().layDanhSachLoaiBaoTri();
            for (String type : dbTypes) {
                if (type != null && !type.trim().isEmpty() && !typesList.contains(type.trim())) {
                    typesList.add(type.trim());
                }
            }
        } catch (Exception ignore) {
        }

        if (typesList.isEmpty()) {
            typesList.add("Vệ sinh phòng");
        }

        cbLoaiBaoTri = new JComboBox<>(typesList.toArray(new String[0]));
        cbLoaiBaoTri.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbLoaiBaoTri.setBackground(Color.WHITE);
        cbLoaiBaoTri.setPreferredSize(new Dimension(280, 38));

        JButton btnAdd = new JButton("+");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAdd.setBackground(new Color(52, 152, 219));
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setFocusPainted(false);
        btnAdd.setPreferredSize(new Dimension(45, 38));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            String newType = JOptionPane.showInputDialog(this, "Nhập loại bảo trì mới:", "Thêm loại",
                    JOptionPane.PLAIN_MESSAGE);
            if (newType != null && !newType.trim().isEmpty()) {
                cbLoaiBaoTri.addItem(newType.trim());
                cbLoaiBaoTri.setSelectedItem(newType.trim());
            }
        });

        JPanel comboPanel = new JPanel(new BorderLayout(8, 0));
        comboPanel.setBackground(Color.WHITE);
        comboPanel.add(cbLoaiBaoTri, BorderLayout.CENTER);
        comboPanel.add(btnAdd, BorderLayout.EAST);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0); // Khoảng cách lớn hơn giữa các block
        panel.add(comboPanel, gbc);

        // 3. Label Mô tả chi tiết
        JLabel lblDesc = new JLabel("Mô tả chi tiết:");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDesc.setForeground(new Color(60, 60, 60));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(lblDesc, gbc);

        // 4. Text Field Mô tả
        txtMoTa = new JTextField();
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtMoTa.setPreferredSize(new Dimension(100, 38));
        txtMoTa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(txtMoTa, gbc);

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

        JButton btnOk = new JButton("Xác nhận");
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnOk.setBackground(new Color(46, 204, 113));
        btnOk.setForeground(Color.WHITE); // Make strictly visible
        btnOk.setPreferredSize(new Dimension(110, 40));
        btnOk.setFocusPainted(false);
        btnOk.setBorderPainted(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> taoBaoTri());

        footer.add(btnCancel);
        footer.add(btnOk);
        add(footer, BorderLayout.SOUTH);
    }

    // Xử lý khi người dùng xác nhận:
    // 1. Lấy dữ liệu từ form (loại bảo trì, mô tả)
    // 2. Tạo đối tượng BaoTriDTO
    // 3. Gọi Controller để lưu vào hệ thống
    // 4. Thông báo thành công và đóng dialog
    private void taoBaoTri() {
        try {
            String moTa = txtMoTa.getText();
            String loaiBaoTri = (String) cbLoaiBaoTri.getSelectedItem();

            BaoTriDTO dto = new BaoTriDTO();
            dto.setMaPhong(phong.getMaPhong());
            dto.setLoaiBaoTri(loaiBaoTri);
            dto.setMoTa(moTa);
            new BaoTriController().taoBaoTri(dto);

            if (onSuccess != null) {
                onSuccess.run();
            }
            dispose();
            JOptionPane.showMessageDialog(getParent(), "PHÒNG ĐANG ĐƯỢC BẢO TRÌ!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo bảo trì: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
