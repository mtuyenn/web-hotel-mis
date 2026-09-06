package com.hotelmanagement.view.phong;

import com.hotelmanagement.model.dto.ChiTietDatPhongDTO;
import com.hotelmanagement.model.dto.DatPhongDTO;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.enums.TinhTrangPhong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lớp RoomCard là thành phần giao diện (UI) dùng để hiển thị
 * thông tin của một phòng dưới dạng thẻ (card).
 * 
 * Mỗi thẻ hiển thị:
 * - Mã phòng
 * - Trạng thái phòng (badge màu sắc)
 * - Thời gian đặt phòng
 * - Loại phòng và giá
 * - Thông tin khách hàng
 * 
 * Thiết kế:
 * - Card có đổ bóng mờ, bo góc, trông hiện đại
 * - Màu sắc thay đổi theo trạng thái phòng
 * - Có hiệu ứng hover (nảy lên khi di chuột vào)
 * - Click vào thẻ sẽ mở RoomActionDialog để xem chi tiết và thực hiện hành động
 */
public class RoomCard extends JPanel {

    private final PhongDTO phong;
    private final Color baseColor;
    private final JPanel dynamicInfoPanel;
    private final JLabel lblTimeRange;

    /**
     * Constructor của RoomCard
     * 
     * @param phong Đối tượng PhongDTO chứa thông tin phòng
     */
    public RoomCard(PhongDTO phong) {
        this.phong = phong;
        this.baseColor = getStatusColor();

        setLayout(new BorderLayout());
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Tăng chiều ngang một chút để tên khách dài hoặc ngày giờ không bị cắt
        Dimension size = new Dimension(265, 290);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        // Background Panel với Shadow mô phỏng Card 3D
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vẽ Đổ bóng mờ nhẹ bên dưới
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(5, 7, getWidth() - 10, getHeight() - 10, 25, 25);

                // Vẽ Card Trắng chính
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 12, 25, 25);

                // Vẽ ảnh trên nửa trên của thẻ
                try {
                    java.net.URL imgUrl = getClass().getResource("/images/rooms/" + phong.getMaPhong() + ".jpg");
                    if (imgUrl != null) {
                        Image img = new ImageIcon(imgUrl).getImage();
                        Shape oldClip = g2.getClip();
                        // Giữ bo góc cho vùng ảnh
                        g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth() - 10, getHeight() - 12, 25,
                                25));

                        // Vẽ ảnh cao khoảng 100px
                        g2.drawImage(img, 0, 0, getWidth() - 10, 110, null);

                        // Overlay màu trạng thái phủ mờ lên ảnh (Tint overlay)
                        g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 160));
                        g2.fillRect(0, 0, getWidth() - 10, 110);

                        g2.setClip(oldClip);
                    }
                } catch (Exception ex) {
                    // Nếu lỗi (thiếu ảnh) thì bỏ qua, chỉ hiển thị card trắng
                }
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(container, BorderLayout.CENTER);

        // ================= HEADER =================
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 22, 10, 22));

        JLabel lblMa = new JLabel(phong.getMaPhong());
        lblMa.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblMa.setForeground(Color.WHITE);
        header.add(lblMa, BorderLayout.WEST);

        // Panel bọc chung Badge và TimeRange
        JPanel rightInfoPanel = new JPanel();
        rightInfoPanel.setLayout(new BoxLayout(rightInfoPanel, BoxLayout.Y_AXIS));
        rightInfoPanel.setOpaque(false);

        // Status Badge bo góc nằm góc trên bên phải
        JLabel badge = new JLabel(getStatusText(), SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(baseColor);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(5, 12, 5, 12));
        badge.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badge);
        badgeWrapper.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightInfoPanel.add(badgeWrapper);

        // Label thời gian Check-in -> Check-out (sẽ được set text sau)
        lblTimeRange = new JLabel(" ");
        lblTimeRange.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTimeRange.setForeground(Color.WHITE);
        lblTimeRange.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel timeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        timeWrapper.setOpaque(false);
        timeWrapper.add(lblTimeRange);
        timeWrapper.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightInfoPanel.add(Box.createVerticalStrut(6));
        rightInfoPanel.add(timeWrapper);

        header.add(rightInfoPanel, BorderLayout.EAST);

        container.add(header, BorderLayout.NORTH);

        // ================= BODY =================
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(5, 22, 15, 32)); // Lề phải chừa lại vì có đổ bóng

        // Cấu hình cố định: Loại phòng và Giá
        body.add(createLine("Loại phòng:", phong.getTenLoaiPhong() != null ? phong.getTenLoaiPhong() : "Chưa xác định",
                false));
        body.add(Box.createVerticalStrut(12));
        body.add(createLine("Giá:", String.format("%,.0f ₫", phong.getGia()), true));
        body.add(Box.createVerticalStrut(18));

        // Đường kẽ ngang mờ chia đôi phần Thông số tĩnh và động
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(235, 235, 235));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        body.add(sep);
        body.add(Box.createVerticalStrut(18));

        // Panel Động chứa Thông tin Khách hàng (sẽ gọi DB)
        dynamicInfoPanel = new JPanel();
        dynamicInfoPanel.setOpaque(false);
        dynamicInfoPanel.setLayout(new BoxLayout(dynamicInfoPanel, BoxLayout.Y_AXIS));
        body.add(dynamicInfoPanel);

        if (phong.getTinhTrang() == TinhTrangPhong.DANG_O || phong.getTinhTrang() == TinhTrangPhong.DA_DAT) {
            JLabel lblLoading = new JLabel("Đang tải dữ liệu khách...");
            lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblLoading.setForeground(new Color(150, 150, 150));
            dynamicInfoPanel.add(lblLoading);
            fetchBookingInfoAsync();
        } else {
            JLabel lblEmpty = new JLabel(
                    "<html><div style='text-align: center; color: #888888;'>Phòng đang trống<br>sẵn sàng nhận khách</div></html>");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            dynamicInfoPanel.add(lblEmpty);
        }

        container.add(body, BorderLayout.CENTER);

        // ================= HOVER =================
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setLocation(getX(), getY() - 5); // Nảy thẻ lên cao chút xíu
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                setLocation(getX(), getY() + 5);
            }
        });
    }

    private JPanel createLine(String title, String value, boolean isPrice) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26)); // Fixed height constraint cho BoxLayout

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(110, 110, 110));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", isPrice ? Font.BOLD : Font.BOLD, isPrice ? 17 : 14));
        if (isPrice) {
            lblValue.setForeground(new Color(44, 62, 80)); // Màu chữ tiền đậm cá tính hơn
        } else {
            lblValue.setForeground(new Color(30, 30, 30));
        }

        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(lblValue, BorderLayout.EAST);
        return panel;
    }

    /**
     * Dùng SwingWorker gọi DAO ngầm tìm người đang thuê phòng
     */
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

                    dynamicInfoPanel.removeAll();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

                    if (dp != null && ct != null) {
                        String name = dp.getTenKH() != null ? dp.getTenKH() : "Khách vãng lai";
                        dynamicInfoPanel.add(createLine("Khách:", name, false));
                        dynamicInfoPanel.add(Box.createVerticalStrut(10));

                        if (phong.getTinhTrang() == TinhTrangPhong.DANG_O) {
                            String out = ct.getNgayTra() != null ? ct.getNgayTra().format(dtf) : "N/A";
                            dynamicInfoPanel.add(createLine("Ngày out:", out, false));
                        } else if (phong.getTinhTrang() == TinhTrangPhong.DA_DAT) {
                            String in = ct.getNgayNhan() != null ? ct.getNgayNhan().format(dtf) : "N/A";
                            dynamicInfoPanel.add(createLine("Dự kiến in:", in, false));
                        }

                        if (ct.getNgayNhan() != null && ct.getNgayTra() != null) {
                            DateTimeFormatter shortDtf = DateTimeFormatter.ofPattern("dd/MM");
                            lblTimeRange.setText(
                                    ct.getNgayNhan().format(shortDtf) + " - " + ct.getNgayTra().format(shortDtf));
                        }
                    } else {
                        // Cấu hình nhạy lỗi fallback
                        JLabel lblErr = new JLabel("Lỗi đồng bộ dữ liệu");
                        lblErr.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                        lblErr.setForeground(Color.RED);
                        dynamicInfoPanel.add(lblErr);
                    }

                    dynamicInfoPanel.revalidate();
                    dynamicInfoPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    dynamicInfoPanel.removeAll();
                    JLabel lblErr = new JLabel("Lỗi truy xuất: " + e.getMessage());
                    lblErr.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    lblErr.setForeground(Color.RED);
                    dynamicInfoPanel.add(lblErr);
                    dynamicInfoPanel.revalidate();
                    dynamicInfoPanel.repaint();
                }
            }
        };
        worker.execute();
    }

    private Color getStatusColor() {
        return switch (phong.getTinhTrang()) {
            case SAN_SANG -> new Color(46, 204, 113);
            case DANG_O -> new Color(231, 76, 60);
            case DANG_DON_DEP -> new Color(149, 165, 166);
            case BAO_TRI -> new Color(241, 196, 15);
            case DA_DAT -> new Color(52, 152, 219);
            default -> Color.GRAY;
        };
    }

    private String getStatusText() {
        return switch (phong.getTinhTrang()) {
            case SAN_SANG -> "TRỐNG";
            case DANG_O -> "CÓ KHÁCH";
            case DANG_DON_DEP -> "ĐANG DỌN";
            case BAO_TRI -> "BẢO TRÌ";
            case DA_DAT -> "ĐÃ ĐẶT";
            default -> phong.getTinhTrang().name();
        };
    }
}