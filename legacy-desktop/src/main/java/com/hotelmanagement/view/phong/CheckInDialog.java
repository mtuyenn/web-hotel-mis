package com.hotelmanagement.view.phong;

import com.hotelmanagement.controller.DatPhongController;
import com.hotelmanagement.controller.KhachHangController;
import com.hotelmanagement.controller.PhongController;
import com.hotelmanagement.model.dto.ChiTietDatPhongDTO;
import com.hotelmanagement.model.dto.DatPhongDTO;
import com.hotelmanagement.model.dto.KhachHangDTO;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.view.AppIcon;
import com.hotelmanagement.view.khachhang.KhachHangDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Lớp CheckInDialog là giao diện (UI) dùng để xử lý chức năng
 * đặt phòng (booking) và nhận phòng (check-in) cho khách hàng.
 * 
 * Lớp này cho phép:
 * - Chọn hoặc thêm mới khách hàng
 * - Nhập thời gian check-in, check-out
 * - Xem lịch đặt phòng của phòng hiện tại
 * - Thực hiện đặt phòng hoặc nhận phòng
 */
public class CheckInDialog extends JDialog {

    private final PhongDTO phong;
    private final boolean isBooking;

    private final KhachHangController khController = new KhachHangController();
    private final DatPhongController dpController = new DatPhongController();
    private List<KhachHangDTO> allKhachHang;

    private JSpinner spinCheckIn;
    private JSpinner spinCheckOut;
    private JLabel lblHinhThucThue;

    // Auto-complete ComboBox thay vì TextField + Table KH
    private JComboBox<String> cbKhachHang;
    private DefaultComboBoxModel<String> cbModelKhachHang;

    // Table lịch sử/booking tương lai của phòng
    private DefaultTableModel modelLichDat;
    private JTable tblLichDat;

    private final Color PRIMARY_COLOR = new Color(30, 41, 59);
    private final Color GOLD_ACCENT = new Color(212, 175, 55);
    private final Color GREEN_BTN = new Color(46, 204, 113);
    private final Color BLUE_BTN = new Color(52, 152, 219);
    private final Color DB_BG = new Color(245, 247, 250);
    private final Runnable onSuccess;

    // Constructor khởi tạo dialog với thông tin phòng và chế độ (đặt phòng / nhận
    // phòng)
    public CheckInDialog(PhongDTO phong, boolean isBooking, Runnable onSuccess) {
        this.phong = phong;
        this.isBooking = isBooking;
        this.onSuccess = onSuccess;

        String actionTitle = isBooking ? "ĐẶT PHÒNG" : "NHẬN PHÒNG";
        setTitle(actionTitle + " - " + phong.getMaPhong());
        setSize(1000, 720);
        setLocationRelativeTo(null);
        setModal(true);
        getContentPane().setBackground(DB_BG);

        JTabbedPane tab = new JTabbedPane();
        tab.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tab.addTab("THÔNG TIN " + actionTitle, createThongTinPanel());

        add(tab, BorderLayout.CENTER);

        // Mặc định load KH lúc mở form
        loadKhachHangToCombo();
        // Lấy lịch sử/Các booking của phòng này
        loadLichDatPhong();
    }

    // Tạo giao diện chính gồm:
    // - Thông tin đơn (khách hàng, thời gian)
    // - Bảng lịch đặt phòng
    // - Các nút chức năng (xác nhận, hủy, đổi lịch)
    private JPanel createThongTinPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(DB_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ------- PHẦN 1: THÔNG TIN THỜI GIAN & KHÁCH HÀNG (Top) -------
        JPanel pnlTop = new JPanel(new GridBagLayout());
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createTitledBorder(null, "Thông Tin Đơn", TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 1.0;

        // Dòng 1: Mã đặt phòng & Khách Hàng
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        pnlTop.add(createLabel("Mã đơn:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        JTextField txtMaDat = new JTextField("Hệ thống tự cấp");
        txtMaDat.setEditable(false);
        txtMaDat.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        txtMaDat.setHorizontalAlignment(JTextField.CENTER);
        txtMaDat.setBackground(new Color(240, 240, 240));
        pnlTop.add(txtMaDat, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        pnlTop.add(createLabel("Thiết lập tên khách:"), gbc);

        // Panel chứa Combo KH và nút Thêm
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        JPanel pnlKhachHang = new JPanel(new BorderLayout(5, 0));
        pnlKhachHang.setOpaque(false);

        cbModelKhachHang = new DefaultComboBoxModel<>();
        cbKhachHang = new JComboBox<>(cbModelKhachHang);
        cbKhachHang.setEditable(true);
        cbKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Tính năng auto-complete
        JTextField txtEditor = (JTextField) cbKhachHang.getEditor().getEditorComponent();
        txtEditor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterCombo();
            }

            public void removeUpdate(DocumentEvent e) {
                filterCombo();
            }

            public void changedUpdate(DocumentEvent e) {
                filterCombo();
            }
        });
        txtEditor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cbKhachHang.setPopupVisible(true);
            }
        });

        pnlKhachHang.add(cbKhachHang, BorderLayout.CENTER);

        JButton btnAddKhach = new JButton("Thêm mới", new AppIcon(AppIcon.Type.ADD, Color.BLACK, 16));
        btnAddKhach.setToolTipText("Thêm khách hàng mới");
        btnAddKhach.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddKhach.setBackground(new Color(52, 152, 219));
        btnAddKhach.setForeground(Color.BLACK);
        btnAddKhach.setFocusPainted(false);
        btnAddKhach.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddKhach.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAddKhach.setBackground(new Color(52, 152, 219).darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAddKhach.setBackground(new Color(52, 152, 219));
            }
        });

        btnAddKhach.addActionListener(e -> addNewKhachHang());
        pnlKhachHang.add(btnAddKhach, BorderLayout.EAST);
        pnlTop.add(pnlKhachHang, gbc);

        // Dòng 2: Check-in, Check-out
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        pnlTop.add(createLabel("Giờ Check-in:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        spinCheckIn = createDateSpinner();
        pnlTop.add(spinCheckIn, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        pnlTop.add(createLabel("Giờ Check-out:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        spinCheckOut = createDateSpinner();
        // Cài mặc định check out là ngày mai trưa 12h
        Date tomorrow = Date.from(
                LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).atZone(ZoneId.systemDefault()).toInstant());
        spinCheckOut.setValue(tomorrow);
        pnlTop.add(spinCheckOut, gbc);

        // Dòng 3: Hình thức
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        pnlTop.add(createLabel("Hình thức thuê:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        lblHinhThucThue = new JLabel("Thuê theo ngày");
        lblHinhThucThue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHinhThucThue.setForeground(GOLD_ACCENT);
        pnlTop.add(lblHinhThucThue, gbc);

        // Events update hình thức thuê
        spinCheckIn.addChangeListener(e -> updateHinhThucThue());
        spinCheckOut.addChangeListener(e -> updateHinhThucThue());
        updateHinhThucThue();

        mainPanel.add(pnlTop, BorderLayout.NORTH);

        // ------- PHẦN 2: LỊCH ĐẶT CỦA PHÒNG NÀY (Center) -------
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createTitledBorder(null, "Lịch Đặt Phòng & Đang Ở (" + phong.getMaPhong() + ")",
                        TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR)));

        String[] cols = { "Mã Đơn", "Khách Hàng", "Check-in", "Check-out", "Giá Phòng", "Tình Trạng" };
        modelLichDat = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblLichDat = new JTable(modelLichDat);
        tblLichDat.setRowHeight(30);
        tblLichDat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblLichDat.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblLichDat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ==================== THÊM SỰ KIỆN CLICK VÀO BẢNG LỊCH ====================
        tblLichDat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tblLichDat.getSelectedRow();
                if (row < 0)
                    return;

                try {
                    // Lấy dữ liệu từ bảng
                    String tenKH = modelLichDat.getValueAt(row, 1).toString();
                    String checkInStr = modelLichDat.getValueAt(row, 2).toString();
                    String checkOutStr = modelLichDat.getValueAt(row, 3).toString();

                    // Đổ dữ liệu lên form trên
                    ((JTextField) cbKhachHang.getEditor().getEditorComponent()).setText(tenKH);

                    // Parse ngày giờ
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                    try {
                        LocalDateTime in = LocalDateTime.parse(checkInStr, dtf);
                        LocalDateTime out = LocalDateTime.parse(checkOutStr, dtf);

                        spinCheckIn.setValue(Date.from(in.atZone(ZoneId.systemDefault()).toInstant()));
                        spinCheckOut.setValue(Date.from(out.atZone(ZoneId.systemDefault()).toInstant()));
                    } catch (Exception ignored) {
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        pnlCenter.add(new JScrollPane(tblLichDat), BorderLayout.CENTER);
        mainPanel.add(pnlCenter, BorderLayout.CENTER);

        // ------- PHẦN 3: ACTIONS (South) -------
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlActions.setBackground(DB_BG);

        String btnText = isBooking ? "XÁC NHẬN ĐẶT PHÒNG" : "XÁC NHẬN NHẬN PHÒNG";
        Color actionColor = isBooking ? BLUE_BTN : GREEN_BTN;

        JButton btnXacNhan = new JButton(btnText);
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnXacNhan.setBackground(actionColor);
        btnXacNhan.setForeground(Color.BLACK);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setPreferredSize(new Dimension(220, 45));
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.addActionListener(e -> xacNhanDatNhanPhong());

        JButton btnHuy = new JButton("HỦY BỎ");
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnHuy.setBackground(new Color(231, 76, 60));
        btnHuy.setForeground(Color.BLACK);
        btnHuy.setFocusPainted(false);
        btnHuy.setPreferredSize(new Dimension(120, 45));
        btnHuy.addActionListener(e -> dispose());

        JButton btnCapNhatLich = new JButton("ĐỔI LỊCH");
        btnCapNhatLich.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCapNhatLich.setBackground(new Color(241, 196, 15)); // Vàng
        btnCapNhatLich.setForeground(Color.BLACK);
        btnCapNhatLich.setFocusPainted(false);
        btnCapNhatLich.setPreferredSize(new Dimension(120, 45));
        btnCapNhatLich.addActionListener(e -> doiLichDatPhong());

        JButton btnHuyPhong = new JButton("HỦY PHÒNG");
        btnHuyPhong.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnHuyPhong.setBackground(new Color(230, 126, 34));
        btnHuyPhong.setForeground(Color.BLACK);
        btnHuyPhong.setFocusPainted(false);
        btnHuyPhong.setPreferredSize(new Dimension(130, 45));
        btnHuyPhong.addActionListener(e -> huyMotPhongDatPhong());

        pnlActions.add(btnHuy);
        pnlActions.add(btnHuyPhong);
        pnlActions.add(btnCapNhatLich);
        pnlActions.add(btnXacNhan);

        mainPanel.add(pnlActions, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm");
        editor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setEditor(editor);
        return spinner;
    }

    private void updateHinhThucThue() {
        Date dIn = (Date) spinCheckIn.getValue();
        Date dOut = (Date) spinCheckOut.getValue();

        LocalDateTime in = dIn.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime out = dOut.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        long hours = ChronoUnit.HOURS.between(in, out);
        if (hours < 0) {
            lblHinhThucThue.setText("Lỗi thời gian (Out < In)");
            lblHinhThucThue.setForeground(Color.RED);
        } else if (hours < 18) {
            lblHinhThucThue.setText("Thuê theo giờ");
            lblHinhThucThue.setForeground(new Color(230, 126, 34)); // Orange
        } else {
            lblHinhThucThue.setText("Thuê theo ngày");
            lblHinhThucThue.setForeground(GOLD_ACCENT);
        }
    }

    private boolean isComboFiltering = false;

    private void loadKhachHangToCombo() {
        allKhachHang = khController.layTatCaKhachHang();
        filterCombo();
    }

    // Lọc danh sách khách hàng theo từ khóa nhập vào
    // Hỗ trợ tìm theo tên hoặc CCCD
    private void filterCombo() {
        if (allKhachHang == null || isComboFiltering)
            return;

        JTextField txtEditor = (JTextField) cbKhachHang.getEditor().getEditorComponent();
        String query = txtEditor.getText().toLowerCase();

        SwingUtilities.invokeLater(() -> {
            isComboFiltering = true;
            cbModelKhachHang.removeAllElements();

            int count = 0;
            for (KhachHangDTO kh : allKhachHang) {
                String name = kh.getTenKH() != null ? kh.getTenKH().toLowerCase() : "";
                String cccd = kh.getCccd() != null ? kh.getCccd().toLowerCase() : "";

                if (name.contains(query) || cccd.contains(query)) {
                    // Hiển thị dạng "Tên Khách Hàng - CCCD"
                    String display = (kh.getTenKH() != null ? kh.getTenKH() : "Khách") + " - "
                            + (kh.getCccd() != null ? kh.getCccd() : "Trống CCCD");
                    cbModelKhachHang.addElement(display);
                    count++;
                }
            }
            if (count > 0 && cbKhachHang.isShowing() && cbKhachHang.hasFocus()) {
                cbKhachHang.setPopupVisible(true);
            }
            txtEditor.setText(query); // Giữ lại chữ người dùng đang gõ
            isComboFiltering = false;
        });
    }

    // Load danh sách các đơn đặt phòng của phòng hiện tại
    // Hiển thị lên bảng để người dùng theo dõi lịch sử và booking tương lai
    private void loadLichDatPhong() {
        new SwingWorker<Void, Object[]>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<DatPhongDTO> dps = dpController.layDatPhongChuaThanhToan();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM HH:mm");

                for (DatPhongDTO dp : dps) {
                    if (dp.getChiTietDatPhongs() == null)
                        continue;
                    for (ChiTietDatPhongDTO ct : dp.getChiTietDatPhongs()) {
                        // Lọc đúng phòng này đang xem
                        if (phong.getMaPhong().equals(ct.getMaPhong())) {
                            String tenKH = dp.getTenKH() != null ? dp.getTenKH() : "Khách vãng lai";
                            String in = ct.getNgayNhan() != null ? ct.getNgayNhan().format(dtf) : "N/A";
                            String out = ct.getNgayTra() != null ? ct.getNgayTra().format(dtf) : "N/A";

                            BigDecimal gia = ct.getGiaPhong() != null ? ct.getGiaPhong() : BigDecimal.ZERO;
                            String giaStr = String.format("%,.0f", gia);

                            publish(new Object[] {
                                    dp.getMaDP(),
                                    tenKH,
                                    in,
                                    out,
                                    giaStr,
                                    dp.getTrangThai() != null ? dp.getTrangThai() : "N/A"
                            });
                        }
                    }
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                for (Object[] row : chunks) {
                    modelLichDat.addRow(row);
                }
            }
        }.execute();
    }

    private void addNewKhachHang() {
        KhachHangDialog dialog = new KhachHangDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Thêm Khách Hàng Mới", null);

        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            // Reload lại danh sách khách hàng vào ComboBox
            loadKhachHangToCombo();
            JOptionPane.showMessageDialog(this, "Đã thêm khách hàng mới!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Cập nhật lại thời gian check-in/check-out của đơn đã chọn
    // Có xác nhận trước khi thực hiện
    private void doiLichDatPhong() {
        int selectedRow = tblLichDat.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch đặt phòng trong bảng để đổi lịch!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long maDP = null;
        try {
            maDP = Long.valueOf(modelLichDat.getValueAt(selectedRow, 0).toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        LocalDateTime ngayNhan = ((Date) spinCheckIn.getValue()).toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime ngayTra = ((Date) spinCheckOut.getValue()).toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắn chắn muốn đổi ngày giờ Check-in/Check-out của đơn "
                + maDP + " không?\nLưu ý: Chỉ được đổi tối đa 1 lần!", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                dpController.capNhatThoiGianDatPhong(maDP, phong.getMaPhong(), ngayNhan, ngayTra);
                JOptionPane.showMessageDialog(this, "Cập nhật ngày giờ thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                // Refresh table
                modelLichDat.setRowCount(0);
                loadLichDatPhong();
                if (onSuccess != null)
                    onSuccess.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Xử lý hủy riêng 1 phòng trong đơn đặt phòng
    private void huyMotPhongDatPhong() {
        int selectedRow = tblLichDat.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch đặt phòng trong bảng để hủy!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long maDP = null;
        try {
            maDP = Long.valueOf(modelLichDat.getValueAt(selectedRow, 0).toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắn chắn muốn hủy đặt phòng này "
                + "(Mã đơn: " + maDP + ", Phòng: " + phong.getMaPhong() + ") không?", "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            try {
                dpController.huyPhongDat(maDP, phong.getMaPhong());
                JOptionPane.showMessageDialog(this, "Hủy phòng thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                // Refresh table & parent view
                modelLichDat.setRowCount(0);
                loadLichDatPhong();
                if (onSuccess != null) {
                    onSuccess.run();
                }
                dispose(); // Đóng giao diện vì phòng có thể đã trống hoàn toàn
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Xử lý logic chính:
    // 1. Lấy thông tin khách hàng (cũ hoặc tạo mới)
    // 2. Lấy thời gian check-in/check-out
    // 3. Nếu là đặt phòng:
    // - Tạo đơn đặt phòng mới
    // - Cập nhật trạng thái phòng = ĐÃ ĐẶT
    // 4. Nếu là nhận phòng:
    // - Lấy đơn hiện tại hoặc tạo mới nếu chưa có
    // - Thực hiện check-in
    // 5. Hiển thị kết quả và đóng dialog
    private void xacNhanDatNhanPhong() {
        JTextField editor = (JTextField) cbKhachHang.getEditor().getEditorComponent();
        String input = editor.getText().trim();

        if (input.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc nhập thông tin khách hàng!",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long maKH = null;

            // Tìm khách hàng cũ
            for (KhachHangDTO kh : allKhachHang) {
                String display = (kh.getTenKH() != null ? kh.getTenKH() : "")
                        + " - " + (kh.getCccd() != null ? kh.getCccd() : "");
                if (display.equalsIgnoreCase(input)
                        || (kh.getCccd() != null && kh.getCccd().equalsIgnoreCase(input))
                        || (kh.getTenKH() != null && kh.getTenKH().equalsIgnoreCase(input))) {
                    maKH = kh.getMaKH();
                    break;
                }
            }

            // Tạo khách mới nếu chưa có
            if (maKH == null) {
                KhachHangDTO newKH = new KhachHangDTO();
                newKH.setTenKH(input);
                if (input.contains("-")) {
                    String[] parts = input.split("-", 2);
                    if (parts.length == 2) {
                        newKH.setTenKH(parts[0].trim());
                        newKH.setCccd(parts[1].trim());
                    }
                }
                khController.taoKhachHang(newKH);
                allKhachHang = khController.layTatCaKhachHang();
                maKH = allKhachHang.get(allKhachHang.size() - 1).getMaKH();
            }

            LocalDateTime ngayNhan = ((Date) spinCheckIn.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime ngayTra = ((Date) spinCheckOut.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            boolean success = false;
            PhongController phongController = new PhongController();

            if (isBooking) {
                // ==================== ĐẶT PHÒNG MỚI ====================
                DatPhongDTO dto = new DatPhongDTO();
                dto.setMaKH(maKH);
                dto.setMaNV("NV001");
                dto.setGiaCoc(BigDecimal.ZERO);

                ChiTietDatPhongDTO chiTiet = new ChiTietDatPhongDTO();
                chiTiet.setMaPhong(phong.getMaPhong());
                chiTiet.setNgayNhan(ngayNhan);
                chiTiet.setNgayTra(ngayTra);
                dto.setChiTietDatPhongs(List.of(chiTiet));

                DatPhongDTO saved = dpController.taoDatPhong(dto);
                if (saved != null && saved.getMaDP() != null) {
                    phongController.capNhatTrangThaiPhong(phong.getMaPhong(), TinhTrangPhong.DA_DAT.name());
                    success = true;
                }
            } else {
                Long maDP = null;
                int selectedRow = tblLichDat.getSelectedRow();

                if (selectedRow >= 0) {
                    try {
                        maDP = Long.valueOf(modelLichDat.getValueAt(selectedRow, 0).toString());
                    } catch (Exception ignored) {
                    }
                }

                if (maDP == null) {
                    // Nếu không có đơn nào, tạo đơn mới
                    DatPhongDTO dto = new DatPhongDTO();
                    dto.setMaKH(maKH);
                    dto.setMaNV("NV001");
                    dto.setGiaCoc(BigDecimal.ZERO);

                    ChiTietDatPhongDTO chiTiet = new ChiTietDatPhongDTO();
                    chiTiet.setMaPhong(phong.getMaPhong());
                    chiTiet.setNgayNhan(ngayNhan);
                    chiTiet.setNgayTra(ngayTra);
                    dto.setChiTietDatPhongs(List.of(chiTiet));

                    DatPhongDTO saved = dpController.taoDatPhong(dto);
                    maDP = saved != null ? saved.getMaDP() : null;
                }

                if (maDP != null) {
                    // Check-in → chuyển trạng thái ChiTietDatPhong và Phong
                    dpController.checkIn(maDP, phong.getMaPhong(), LocalDateTime.now());

                    // Phòng update & Hóa đơn update đã được xử lý trong dpController.checkIn()

                    // Phong update moved to inside checkIn method (already there)
                    success = true;
                }
            }
            if (success) {
                String msg = isBooking ? "Đặt phòng thành công!" : "Nhận phòng thành công!";
                JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                if (onSuccess != null)
                    onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Thao tác thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}