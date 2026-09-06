package com.hotelmanagement.view.phong;

import com.hotelmanagement.controller.HoaDonController;
import com.hotelmanagement.model.dto.HoaDonDTO;
import com.hotelmanagement.model.enums.PhuongThucThanhToan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Lớp CheckOutDialog là giao diện (UI) dùng để xử lý chức năng
 * thanh toán và trả phòng cho khách hàng.
 * 
 * Lớp này cho phép:
 * - Hiển thị thông tin hóa đơn (khách hàng, thời gian, chi phí)
 * - Tính toán tổng tiền cần thanh toán
 * - Áp dụng giảm giá (theo % hoặc số tiền)
 * - Chọn phương thức thanh toán
 * - Thực hiện thanh toán và cập nhật trạng thái phòng
 */
public class CheckOutDialog extends JDialog {

    private String maPhong;
    private Long maDP;
    private HoaDonDTO hoaDonDTO;
    private Runnable onSuccess;
    private HoaDonController hoaDonController = new HoaDonController();

    private JComboBox<PhuongThucThanhToan> cmbPhuongThuc;
    private JTextField txtGiamGia;
    private JLabel lblTong;
    private DecimalFormat df = new DecimalFormat("#,###");

    private BigDecimal tongP, tongD, coc;

    // Khởi tạo dialog với thông tin phòng, mã đặt phòng và dữ liệu hóa đơn
    // Đồng thời lấy sẵn các giá trị tiền phòng, dịch vụ và tiền cọc
    public CheckOutDialog(String maPhong, Long maDP, HoaDonDTO hoaDonDTO, Runnable onSuccess) {
        super((Frame) null, "Thanh Toán & Trả Phòng", true);
        this.maPhong = maPhong;
        this.maDP = maDP;
        this.hoaDonDTO = hoaDonDTO;
        this.onSuccess = onSuccess;

        // Fetch numbers
        tongP = hoaDonDTO.getTongTienPhong() != null ? hoaDonDTO.getTongTienPhong() : BigDecimal.ZERO;
        tongD = hoaDonDTO.getTongTienDichVu() != null ? hoaDonDTO.getTongTienDichVu() : BigDecimal.ZERO;
        coc = hoaDonDTO.getTienCocDaDong() != null ? hoaDonDTO.getTienCocDaDong() : BigDecimal.ZERO;

        setSize(480, 560);
        setLocationRelativeTo(null);
        initUI();
        updateTongTienDuKien();
    }

    // Tạo giao diện gồm:
    // - Header: tiêu đề và mã phòng
    // - Body: thông tin khách hàng + bảng chi phí
    // - Footer: nút xác nhận và hủy
    // Có thêm sự kiện để tự động cập nhật tổng tiền khi nhập giảm giá
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(26, 32, 44)); // Dark slate
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("HÓA ĐƠN TRẢ PHÒNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblRoom = new JLabel(maPhong);
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRoom.setForeground(new Color(241, 196, 15)); // Gold

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblRoom, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- BODY ---
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        bodyPanel.setBackground(Color.WHITE);

        // Thông tin khách hàng
        JPanel pnlInfo = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(0, 0, 15, 0)));

        String tenKH = hoaDonDTO.getTenKhachHang() != null ? hoaDonDTO.getTenKhachHang() : "Khách Lẻ";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String in = hoaDonDTO.getNgayNhan() != null ? hoaDonDTO.getNgayNhan().format(dtf) : "--";
        String out = hoaDonDTO.getNgayTra() != null ? hoaDonDTO.getNgayTra().format(dtf) : "--";

        pnlInfo.add(createLabel("Khách hàng: " + tenKH, Font.BOLD));
        pnlInfo.add(createLabel("Mã đơn: " + maDP, Font.PLAIN));
        pnlInfo.add(createLabel("Check-in:  " + in, Font.PLAIN));
        pnlInfo.add(createLabel("Check-out: " + out, Font.PLAIN));

        bodyPanel.add(pnlInfo);
        bodyPanel.add(Box.createVerticalStrut(20));

        // Bảng chi phí
        JPanel pnlChiPhi = new JPanel(new GridBagLayout());
        pnlChiPhi.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.weightx = 0.5;

        // Tiền phòng
        gbc.gridy = 0;
        pnlChiPhi.add(new JLabel("Tổng tiền phòng:"), gbc);
        gbc.gridx = 1;
        pnlChiPhi.add(createRightLabel(df.format(tongP) + " đ"), gbc);

        // Dịch vụ
        gbc.gridy++;
        gbc.gridx = 0;
        pnlChiPhi.add(new JLabel("Tiền dịch vụ (Ăn uống, Giặt ủi):"), gbc);
        gbc.gridx = 1;
        pnlChiPhi.add(createRightLabel(df.format(tongD) + " đ"), gbc);

        // Cọc
        gbc.gridy++;
        gbc.gridx = 0;
        pnlChiPhi.add(new JLabel("Tiền cọc đã giữ (-):"), gbc);
        JLabel lblCoc = createRightLabel("-" + df.format(coc) + " đ");
        lblCoc.setForeground(new Color(231, 76, 60));
        gbc.gridx = 1;
        pnlChiPhi.add(lblCoc, gbc);

        // Giảm giá
        gbc.gridy++;
        gbc.gridx = 0;
        pnlChiPhi.add(new JLabel("Nhập Giảm giá (VND hoặc %):"), gbc);
        txtGiamGia = new JTextField("0");
        txtGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 1;
        pnlChiPhi.add(txtGiamGia, gbc);

        // Đường gạch chéo
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        pnlChiPhi.add(new JSeparator(), gbc);

        // Phương thức thanh toán
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel lblPT = new JLabel("Phương thức thanh toán:");
        lblPT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlChiPhi.add(lblPT, gbc);

        cmbPhuongThuc = new JComboBox<>(PhuongThucThanhToan.values());
        gbc.gridx = 1;
        pnlChiPhi.add(cmbPhuongThuc, gbc);

        bodyPanel.add(pnlChiPhi);
        bodyPanel.add(Box.createVerticalStrut(20));

        // TỔNG CỘNG
        JPanel pnlTotal = new JPanel(new BorderLayout());
        pnlTotal.setBackground(new Color(245, 246, 250)); // Light grey backing
        pnlTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel lblTongTitle = new JLabel("TỔNG THANH TOÁN:");
        lblTongTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblTong = new JLabel("0 đ");
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTong.setForeground(new Color(46, 204, 113)); // Emerald green
        lblTong.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlTotal.add(lblTongTitle, BorderLayout.WEST);
        pnlTotal.add(lblTong, BorderLayout.EAST);
        bodyPanel.add(pnlTotal);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);

        // --- FOOTER BUTTONS ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(Color.WHITE);
        pnlButtons.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnHuy = new JButton("Hủy Bỏ");
        btnHuy.setPreferredSize(new Dimension(100, 35));
        btnHuy.setFocusPainted(false);
        btnHuy.setForeground(Color.BLACK);
        btnHuy.addActionListener(e -> dispose());
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHuy.setBackground(new Color(200, 200, 200));

        JButton btnXacNhan = new JButton("Xác Nhận & Trả Phòng");
        btnXacNhan.setPreferredSize(new Dimension(180, 35));
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setBackground(new Color(46, 204, 113));
        btnXacNhan.setForeground(Color.BLACK);
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXacNhan.addActionListener(e -> processThanhToan());

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnXacNhan);

        mainPanel.add(pnlButtons, BorderLayout.SOUTH);
        add(mainPanel);

        // Events
        txtGiamGia.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateTongTienDuKien();
            }

            public void removeUpdate(DocumentEvent e) {
                updateTongTienDuKien();
            }

            public void changedUpdate(DocumentEvent e) {
                updateTongTienDuKien();
            }
        });
    }

    private JLabel createLabel(String text, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, 13));
        return label;
    }

    private JLabel createRightLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    // Tính toán tổng tiền cần thanh toán:
    // 1. Lấy tổng tiền phòng + dịch vụ
    // 2. Áp dụng giảm giá (theo % nếu <=100, ngược lại là số tiền)
    // 3. Giới hạn giảm giá tối đa 30%
    // 4. Trừ tiền cọc đã thanh toán
    // 5. Đảm bảo tổng tiền >= 0
    // 6. Hiển thị kết quả lên giao diện
    private void updateTongTienDuKien() {
        try {
            String txt = txtGiamGia.getText().trim().replace(",", "");
            if (txt.isEmpty())
                txt = "0";
            BigDecimal giamGia = new BigDecimal(txt);

            BigDecimal tongTruocGiam = tongP.add(tongD);
            BigDecimal tienGiam = BigDecimal.ZERO;

            if (giamGia.compareTo(BigDecimal.valueOf(100)) <= 0) {
                tienGiam = tongTruocGiam.multiply(giamGia.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            } else {
                tienGiam = giamGia;
            }

            // Giảm giá tối đa 30%
            BigDecimal limit = tongTruocGiam.multiply(BigDecimal.valueOf(0.3));
            if (tienGiam.compareTo(limit) > 0) {
                tienGiam = limit;
                txtGiamGia.setForeground(Color.RED);
            } else {
                txtGiamGia.setForeground(Color.BLACK);
            }

            BigDecimal phaiTra = tongTruocGiam.subtract(tienGiam).subtract(coc);
            if (phaiTra.compareTo(BigDecimal.ZERO) < 0)
                phaiTra = BigDecimal.ZERO;

            lblTong.setText(df.format(phaiTra) + " đ");
        } catch (Exception e) {
            lblTong.setText("Lỗi");
        }
    }

    // Xử lý khi người dùng xác nhận thanh toán:
    // 1. Lấy phương thức thanh toán và giảm giá
    // 2. Gọi Controller để thực hiện thanh toán
    // 3. Hiển thị thông báo thành công
    // 4. Đóng dialog và refresh giao diện
    private void processThanhToan() {
        try {
            PhuongThucThanhToan pt = (PhuongThucThanhToan) cmbPhuongThuc.getSelectedItem();
            String txt = txtGiamGia.getText().trim().replace(",", "");
            BigDecimal giamGia = txt.isEmpty() ? BigDecimal.ZERO : new BigDecimal(txt);

            // Execute service logic
            hoaDonController.thanhToan(maDP, pt, giamGia);

            JOptionPane.showMessageDialog(this,
                    "Thanh toán hoàn tất! Phòng " + maPhong + " đã cập nhật trạng thái trống (Đang dọn dẹp).",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            dispose();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Số tiền giảm giá không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtGiamGia.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý trả phòng:\n" + ex.getMessage(), "Thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
