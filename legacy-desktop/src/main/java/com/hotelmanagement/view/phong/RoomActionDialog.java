package com.hotelmanagement.view.phong;

import com.hotelmanagement.model.dto.ChiTietDatPhongDTO;
import com.hotelmanagement.model.dto.DatPhongDTO;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.controller.DatPhongController;
import com.hotelmanagement.view.AppIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lớp RoomActionDialog là giao diện (UI) dùng để hiển thị
 * chi tiết thông tin phòng và thực hiện các hành động liên quan.
 * 
 * Lớp này cho phép:
 * - Xem thông tin chi tiết phòng (loại phòng, giá, mô tả)
 * - Xem thông tin đặt phòng hiện tại (nếu có)
 * - Thực hiện các hành động: ĐẶT PHÒNG, NHẬN PHÒNG, TRẢ PHÒNG, DỊCH VỤ, BẢO TRÌ
 * - Giao diện đẹp, hiện đại với màu sắc theo trạng thái phòng
 */
public class RoomActionDialog extends JDialog {

    private final JPanel gridInfoPanel;
    private final PhongDTO phong;
    private final DatPhongController datPhongController = new DatPhongController();
    private final Runnable onSuccess;

    /**
     * Constructor của RoomActionDialog
     * 
     * @param phong     Đối tượng PhongDTO chứa thông tin phòng
     * @param ngayXem   Ngày hiện tại (dùng để kiểm tra trạng thái đặt phòng)
     * @param onSuccess Runnable callback để refresh giao diện sau khi thực hiện
     *                  hành động
     */
    public RoomActionDialog(PhongDTO phong, LocalDate ngayXem, Runnable onSuccess) {
        this.phong = phong;
        this.onSuccess = onSuccess;
        setTitle("Chi tiết phòng " + phong.getMaPhong());
        setSize(650, 480);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ================= HEADER: Bao phủ ảnh nền nền cực đẹp =================
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                try {
                    java.net.URL imgUrl = getClass().getResource("/images/rooms/" + phong.getMaPhong() + ".jpg");
                    if (imgUrl != null) {
                        Image img = new ImageIcon(imgUrl).getImage();
                        g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);

                        // Lớp phủ màu mờ theo trạng thái để làm nổi bật text
                        Color statusColor = getStatusColor(phong);
                        g2.setColor(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 160));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }
                } catch (Exception ex) {
                    // Mặc định thì super sẽ vẽ background color xanh/đỏ...
                }
            }
        };
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(getStatusColor(phong)); // Default if no image
        headerPanel.setBorder(new EmptyBorder(35, 20, 30, 20));

        JLabel lblRoom = new JLabel("PHÒNG " + phong.getMaPhong());
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblRoom.setForeground(Color.WHITE);
        lblRoom.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStatus = new JLabel(getStatusText(phong).toUpperCase());
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblStatus.setForeground(new Color(255, 255, 255, 230));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 1, true),
                new EmptyBorder(6, 25, 6, 25)));

        headerPanel.add(lblRoom);
        headerPanel.add(Box.createVerticalStrut(15));
        headerPanel.add(lblStatus);

        add(headerPanel, BorderLayout.NORTH);

        // ================= CONTENT: Grid layout thông minh =================
        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.setBackground(Color.WHITE);
        centerContent.setBorder(new EmptyBorder(30, 40, 30, 40));

        gridInfoPanel = new JPanel(new GridLayout(0, 2, 40, 25)); // 2 cột, lề phải trái 40, dòng cách nhau 25
        gridInfoPanel.setBackground(Color.WHITE);

        // Nạp thông tin tĩnh
        renderStaticInfo();

        centerContent.add(gridInfoPanel, BorderLayout.NORTH);
        add(centerContent, BorderLayout.CENTER);

        // ================= FOOTER BUTTONS =================
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)));

        Dimension bigBtn = new Dimension(170, 48);
        Dimension smallBtn = new Dimension(135, 44);

        if (phong.getTinhTrang() == TinhTrangPhong.SAN_SANG) {
            footerPanel.add(createButton("ĐẶT PHÒNG", smallBtn, new Color(52, 152, 219), e -> {
                dispose();
                new CheckInDialog(phong, true, () -> {
                    if (onSuccess != null)
                        onSuccess.run(); // refresh từ MainRoomDashboardView
                }).setVisible(true);
            }));
            footerPanel.add(createButton("NHẬN PHÒNG", smallBtn, new Color(46, 204, 113), e -> {
                dispose();
                new CheckInDialog(phong, false, () -> {
                    if (onSuccess != null)
                        onSuccess.run(); // refresh từ MainRoomDashboardView
                }).setVisible(true);
            }));
            footerPanel.add(createButton("BẢO TRÌ", smallBtn, new Color(241, 196, 15), e -> {
                new CreateBaoTriDialog(this, phong, onSuccess).setVisible(true);
                dispose();
            }));
        } else if (phong.getTinhTrang() == TinhTrangPhong.DA_DAT) {
            footerPanel.add(createButton("ĐẶT PHÒNG", smallBtn, new Color(52, 152, 219), e -> {
                dispose();
                new CheckInDialog(phong, true, () -> {
                    if (onSuccess != null)
                        onSuccess.run(); // refresh từ MainRoomDashboardView
                }).setVisible(true);
            }));
            footerPanel.add(createButton("NHẬN PHÒNG", smallBtn, new Color(46, 204, 113), e -> {
                dispose();
                new CheckInDialog(phong, false, () -> {
                    if (onSuccess != null)
                        onSuccess.run(); // refresh từ MainRoomDashboardView
                }).setVisible(true);
            }));

        } else if (phong.getTinhTrang() == TinhTrangPhong.DANG_O) {
            footerPanel.add(createButton("ĐẶT PHÒNG", smallBtn, new Color(52, 152, 219), e -> {
                dispose();
                new CheckInDialog(phong, true, () -> {
                    if (onSuccess != null)
                        onSuccess.run(); // refresh từ MainRoomDashboardView
                }).setVisible(true);
            }));
            footerPanel.add(createButton("C.PHÒNG", smallBtn, new Color(241, 196, 15), e -> {
                try {
                    java.util.List<com.hotelmanagement.model.dto.DatPhongDTO> dps = new com.hotelmanagement.controller.DatPhongController()
                            .layDatPhongChuaThanhToan();
                    Long maDpToTransfer = null;
                    for (com.hotelmanagement.model.dto.DatPhongDTO dp : dps) {
                        if (dp.getChiTietDatPhongs() != null) {
                            for (var ct : dp.getChiTietDatPhongs()) {
                                if (ct.getMaPhong().equals(phong.getMaPhong())) {
                                    maDpToTransfer = dp.getMaDP();
                                    break;
                                }
                            }
                        }
                    }
                    if (maDpToTransfer != null) {
                        new ChuyenPhongDialog(this, maDpToTransfer, phong, onSuccess).setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Phòng này chưa có dữ liệu đặt phòng hợp lệ!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }));
            footerPanel.add(createButton("D.VỤ", smallBtn, new Color(139, 69, 19), e -> {
                try {
                    java.util.List<com.hotelmanagement.model.dto.DatPhongDTO> dps = new com.hotelmanagement.controller.DatPhongController()
                            .layDatPhongChuaThanhToan();
                    Long maDpToService = null;
                    for (com.hotelmanagement.model.dto.DatPhongDTO dp : dps) {
                        if (dp.getChiTietDatPhongs() != null) {
                            for (var ct : dp.getChiTietDatPhongs()) {
                                if (ct.getMaPhong().equals(phong.getMaPhong())) {
                                    maDpToService = dp.getMaDP();
                                    break;
                                }
                            }
                        }
                    }
                    if (maDpToService != null) {
                        new ServiceDialog(this, maDpToService, onSuccess).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Phòng này chưa có dữ liệu đặt phòng hợp lệ!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }));
            footerPanel.add(createButton("TRẢ PHÒNG", smallBtn, new Color(231, 76, 60), e -> {
                try {
                    java.util.List<com.hotelmanagement.model.dto.DatPhongDTO> dps = datPhongController
                            .layDatPhongChuaThanhToan();
                    Long maDpToCancel = null;
                    for (com.hotelmanagement.model.dto.DatPhongDTO dp : dps) {
                        if (dp.getChiTietDatPhongs() != null) {
                            for (var ct : dp.getChiTietDatPhongs()) {
                                if (ct.getMaPhong().equals(phong.getMaPhong())) {
                                    maDpToCancel = dp.getMaDP();
                                    break;
                                }
                            }
                        }
                    }
                    if (maDpToCancel != null) {
                        int opt = JOptionPane.showConfirmDialog(this,
                                "Bạn có chắn chắn muốn trả phòng " + phong.getMaPhong() + "?", "Xác nhận",
                                JOptionPane.YES_NO_OPTION);
                        if (opt == JOptionPane.YES_OPTION) {
                            dispose();
                            com.hotelmanagement.model.dto.HoaDonDTO hd = new com.hotelmanagement.controller.HoaDonController()
                                    .timHoaDonTheoDatPhong(maDpToCancel);
                            new CheckOutDialog(phong.getMaPhong(), maDpToCancel, hd, () -> {
                                if (onSuccess != null)
                                    onSuccess.run();
                            }).setVisible(true);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }));
            footerPanel.add(createButton("D.VỤ", smallBtn, new Color(155, 89, 182), e -> {
            }));
        } else if (phong.getTinhTrang() == TinhTrangPhong.DANG_DON_DEP) {
            footerPanel.add(createButton("SẴN SÀNG", smallBtn, new Color(46, 204, 113), e -> {
                new com.hotelmanagement.controller.PhongController().capNhatTrangThaiPhong(phong.getMaPhong(),
                        TinhTrangPhong.SAN_SANG.name());
                if (onSuccess != null)
                    onSuccess.run();
                dispose();
            }));
            footerPanel.add(createButton("BẢO TRÌ", smallBtn, new Color(241, 196, 15), e -> {
                new CreateBaoTriDialog(this, phong, onSuccess).setVisible(true);
                dispose();
            }));
        } else if (phong.getTinhTrang() == TinhTrangPhong.BAO_TRI) {
            footerPanel.add(createButton("HOÀN THÀNH", bigBtn, new Color(46, 204, 113), e -> {
                try {
                    com.hotelmanagement.controller.BaoTriController btCtrl = new com.hotelmanagement.controller.BaoTriController();
                    java.util.List<com.hotelmanagement.model.dto.BaoTriDTO> listBT = btCtrl
                            .layDanhSachBaoTri(phong.getMaPhong());
                    for (com.hotelmanagement.model.dto.BaoTriDTO bt : listBT) {
                        if (bt.getTinhTrangBTri() == com.hotelmanagement.model.enums.TinhTrangBaoTri.DANG_BAO_TRI) {
                            btCtrl.capNhatTrangThaiBaoTri(bt.getMaBT(),
                                    com.hotelmanagement.model.enums.TinhTrangBaoTri.DA_HOAN_THANH.name(),
                                    "Đã xử lý xong tác vụ");
                        }
                    }

                    new com.hotelmanagement.controller.PhongController().capNhatTrangThaiPhong(phong.getMaPhong(),
                            TinhTrangPhong.SAN_SANG.name());

                    JOptionPane.showMessageDialog(this, "Bảo trì hoàn tất. Phòng đã SẴN SÀNG !", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    if (onSuccess != null)
                        onSuccess.run();
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi hoàn thành bảo trì: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }));
        }

        add(footerPanel, BorderLayout.SOUTH);

        // Nạp data booking ngầm
        if (phong.getTinhTrang() == TinhTrangPhong.DANG_O || phong.getTinhTrang() == TinhTrangPhong.DA_DAT) {
            fetchBookingInfoAsync();
        }
    }

    // Render thông tin tĩnh của phòng:
    // - Loại phòng
    // - Giá phòng
    private void renderStaticInfo() {
        gridInfoPanel.add(createInfoRow("Loại phòng",
                phong.getTenLoaiPhong() != null ? phong.getTenLoaiPhong() : "Chưa xác định", AppIcon.Type.TAG));
        gridInfoPanel
                .add(createInfoRow("Giá phòng", String.format("%,.0f ₫ / Ngày", phong.getGia()), AppIcon.Type.MONEY));
    }

    // Tạo một hàng thông tin gồm: icon + nhãn + giá trị
    // Icon được lấy từ AppIcon
    // Giá trị được định dạng (ví dụ: tiền tệ)
    private JPanel createInfoRow(String label, String value, AppIcon.Type type) {
        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setBackground(Color.WHITE);

        // Ráp thẳng icon vào JLabel để tận dụng khả năng căn nền mượt mà của Swing
        JLabel lblTitle = new JLabel(label);
        lblTitle.setIcon(AppIcon.of(type, new Color(130, 130, 130), 16));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(140, 140, 140));
        lblTitle.setIconTextGap(8); // Khoảng cách giữa icon và chữ

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblValue.setForeground(new Color(45, 55, 72));
        lblValue.setBorder(new EmptyBorder(0, 24, 0, 0)); // Thụt lề trị số canh đều với text, nhường chỗ cho width của
                                                          // icon

        row.add(lblTitle, BorderLayout.NORTH);
        row.add(lblValue, BorderLayout.CENTER);

        return row;
    }

    // Lấy thông tin đặt phòng bất đồng bộ (sử dụng SwingWorker)
    // Để tránh làm treo giao diện khi gọi database
    private void fetchBookingInfoAsync() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() {
                try {
                    com.hotelmanagement.model.dao.ChiTietDatPhongDAO dao = new com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl();
                    List<com.hotelmanagement.model.entity.ChiTietDatPhong> cacChiTiet = dao
                            .findByPhong(phong.getMaPhong());

                    com.hotelmanagement.model.entity.ChiTietDatPhong matched = cacChiTiet.stream()
                            .filter(c -> c.getTrangThai() == phong.getTinhTrang()
                                    || c.getTrangThai() == com.hotelmanagement.model.enums.TinhTrangPhong.DANG_O
                                    || c.getTrangThai() == com.hotelmanagement.model.enums.TinhTrangPhong.DA_DAT)
                            .findFirst().orElse(null);

                    if (matched != null && matched.getDatPhong() != null) {
                        com.hotelmanagement.model.entity.DatPhong fullDatPhong = dao
                                .findByMaDPWithFullDetails(matched.getDatPhong().getMaDP());
                        if (fullDatPhong != null) {
                            com.hotelmanagement.model.entity.ChiTietDatPhong fullyFetchedCtp = fullDatPhong
                                    .getChiTietDatPhongs().stream()
                                    .filter(c -> c.getPhong().getMaPhong().equals(phong.getMaPhong()))
                                    .findFirst().orElse(null);

                            DatPhongDTO dpDTO = com.hotelmanagement.mapper.Mapper.toDatPhongDTO(fullDatPhong);
                            ChiTietDatPhongDTO ctpDTO = com.hotelmanagement.mapper.Mapper
                                    .toChiTietDatPhongDTO(fullyFetchedCtp != null ? fullyFetchedCtp : matched);
                            return new Object[] { dpDTO, ctpDTO };
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return new Object[] { null, null };
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    DatPhongDTO dp = (DatPhongDTO) result[0];
                    ChiTietDatPhongDTO ct = (ChiTietDatPhongDTO) result[1];

                    if (dp != null && ct != null) {
                        gridInfoPanel.removeAll();
                        renderStaticInfo(); // Load lại loại và giá phong đầu tiên

                        DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("HH:mm");

                        String name = dp.getTenKH() != null ? dp.getTenKH() : "Khách vãng lai";
                        gridInfoPanel.add(createInfoRow("Khách hàng", name, AppIcon.Type.USER));

                        if (phong.getTinhTrang() == TinhTrangPhong.DANG_O) {
                            long days = 1;
                            if (ct.getNgayNhan() != null && ct.getNgayTra() != null) {
                                days = java.time.temporal.ChronoUnit.DAYS.between(ct.getNgayNhan().toLocalDate(),
                                        ct.getNgayTra().toLocalDate());
                                if (days <= 0)
                                    days = 1;
                            }
                            java.math.BigDecimal tongTien = java.math.BigDecimal.ZERO;
                            if (ct.getGiaPhong() != null) {
                                tongTien = ct.getGiaPhong().multiply(new java.math.BigDecimal(days));
                            }
                            String tongTienStr = String.format("%,.0f ₫", tongTien);
                            gridInfoPanel.add(createInfoRow("Tổng tiền", tongTienStr, AppIcon.Type.MONEY));

                            String in = ct.getNgayNhan() != null
                                    ? ct.getNgayNhan().format(dtfDate) + " " + ct.getNgayNhan().format(dtfTime)
                                    : "N/A";
                            String out = ct.getNgayTra() != null
                                    ? ct.getNgayTra().format(dtfDate) + " " + ct.getNgayTra().format(dtfTime)
                                    : "N/A";
                            gridInfoPanel.add(createInfoRow("Check-in", in, AppIcon.Type.CHECKIN));
                            gridInfoPanel.add(createInfoRow("Check-out", out, AppIcon.Type.CHECKOUT));
                        } else if (phong.getTinhTrang() == TinhTrangPhong.DA_DAT) {
                            String ngayDat = dp.getNgayDat() != null ? dp.getNgayDat().format(dtfDate) : "N/A";
                            gridInfoPanel.add(createInfoRow("Ngày đặt", ngayDat, AppIcon.Type.CALENDAR));

                            String in = ct.getNgayNhan() != null
                                    ? ct.getNgayNhan().format(dtfDate) + " " + ct.getNgayNhan().format(dtfTime)
                                    : "N/A";
                            gridInfoPanel.add(createInfoRow("Dự kiến Check-in", in, AppIcon.Type.CHECKIN));
                        }

                        gridInfoPanel.revalidate();
                        gridInfoPanel.repaint();

                        // Giãn chiều cao form nếu lưới Grid tràn
                        setSize(getWidth(), 600);
                    }
                } catch (Exception e) {
                }
            }
        };
        worker.execute();
    }

    private JButton createButton(String text, Dimension size, Color bg, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(size);
        btn.setFocusPainted(false);
        // Tắt Border mặc định để giữ màu nguyên khối Flat Background
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(2, 2, 2, 2)); // Fix lỗi text chữ bị cắt nếu thiếu diện tích
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    // Lấy màu nền tương ứng với trạng thái phòng
    private Color getStatusColor(PhongDTO p) {
        return switch (p.getTinhTrang()) {
            case SAN_SANG -> new Color(46, 204, 113);
            case DANG_O -> new Color(231, 76, 60);
            case DANG_DON_DEP -> new Color(149, 165, 166);
            case BAO_TRI -> new Color(241, 196, 15);
            case DA_DAT -> new Color(52, 152, 219);
            default -> Color.GRAY;
        };
    }

    // Lấy text hiển thị tương ứng với trạng thái phòng
    private String getStatusText(PhongDTO p) {
        return switch (p.getTinhTrang()) {
            case SAN_SANG -> "TRỐNG";
            case DANG_O -> "CÓ KHÁCH";
            case DANG_DON_DEP -> "ĐANG DỌN";
            case BAO_TRI -> "BẢO TRÌ";
            case DA_DAT -> "ĐÃ ĐẶT";
            default -> p.getTinhTrang().name();
        };
    }
}
