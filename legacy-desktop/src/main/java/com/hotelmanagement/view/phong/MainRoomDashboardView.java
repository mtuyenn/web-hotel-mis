package com.hotelmanagement.view.phong;

import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.service.PhongService;
import com.hotelmanagement.view.BaseView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import com.hotelmanagement.service.impl.PhongServiceImpl;

/**
 * Lớp MainRoomDashboardView là giao diện chính dùng để hiển thị
 * và quản lý danh sách phòng trong hệ thống khách sạn.
 * 
 * Lớp này cho phép:
 * - Hiển thị danh sách phòng dưới dạng grid (card)
 * - Lọc phòng theo tầng, ngày và trạng thái
 * - Thực hiện các thao tác như đặt phòng, check-in, check-out thông qua dialog
 */
public class MainRoomDashboardView extends BaseView {

    private final PhongService phongService = new PhongServiceImpl();

    private JPanel roomGridPanel;
    private JComboBox<String> cbTang;
    private JTextField txtNgayXem;
    private JButton btnChonNgay;

    // 6 nút trạng thái
    private JButton btnAll, btnTrong, btnCoKhach, btnDangDon, btnBaoTri, btnDaDat;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MainRoomDashboardView() {
        super();
        setName("Quản Lý Phòng");
        // Override một phần BaseView để phù hợp với Dashboard
        customizeBaseView();

        initTopFilterPanel();
        initRoomGrid();
        loadData(LocalDate.now());
    }

    /** Tùy chỉnh lại BaseView cho phù hợp với Dashboard (ẩn bớt nút CRUD) */
    private void customizeBaseView() {
        // Ẩn các nút CRUD mặc định của BaseView vì dùng filter riêng
        for (Component c : getComponents()) {
            if (c instanceof JPanel top && top.getComponentCount() > 0) {
                // Có thể ẩn btnAdd, btnEdit... nếu cần
            }
        }
    }

    // Tạo thanh filter phía trên gồm:
    // - Chọn tầng
    // - Chọn ngày
    // - Các nút lọc theo trạng thái phòng (trống, có khách, bảo trì,...)
    private void initTopFilterPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        top.setBackground(new Color(248, 249, 250));
        top.setBorder(BorderFactory.createEmptyBorder(5, 15, 50, 15));

        top.add(new JLabel("Tầng:"));
        cbTang = new JComboBox<>(new String[] { "Tất cả tầng", "Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng 5" });
        cbTang.setPreferredSize(new Dimension(145, 36));
        cbTang.addActionListener(e -> filterRooms());
        top.add(cbTang);

        top.add(new JLabel("   Ngày xem:"));
        txtNgayXem = new JTextField(11);
        txtNgayXem.setEditable(false);
        txtNgayXem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNgayXem.setBackground(Color.WHITE);
        txtNgayXem.setText(LocalDate.now().format(formatter));
        top.add(txtNgayXem);

        btnChonNgay = new JButton("📅 Chọn ngày");
        btnChonNgay.setPreferredSize(new Dimension(120, 36));
        btnChonNgay.addActionListener(e -> chonNgay());
        top.add(btnChonNgay);

        // 6 nút trạng thái
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusPanel.setOpaque(false);

        btnAll = createStatusButton("Tất cả", new Color(52, 73, 94));
        btnTrong = createStatusButton("Trống", new Color(46, 204, 113));
        btnCoKhach = createStatusButton("Có khách", new Color(231, 76, 60));
        btnDangDon = createStatusButton("Đang dọn", new Color(149, 165, 166));
        btnBaoTri = createStatusButton("Bảo trì", new Color(241, 196, 15));
        btnDaDat = createStatusButton("Đã đặt", new Color(52, 152, 219));

        statusPanel.add(btnAll);
        statusPanel.add(btnTrong);
        statusPanel.add(btnCoKhach);
        statusPanel.add(btnDangDon);
        statusPanel.add(btnBaoTri);
        statusPanel.add(btnDaDat);

        top.add(statusPanel);
        add(top, BorderLayout.NORTH);

        // Sự kiện
        btnAll.addActionListener(e -> loadData(getSelectedDate()));
        btnTrong.addActionListener(e -> filterByStatus(TinhTrangPhong.SAN_SANG));
        btnCoKhach.addActionListener(e -> filterByStatus(TinhTrangPhong.DANG_O));
        btnDangDon.addActionListener(e -> filterByStatus(TinhTrangPhong.DANG_DON_DEP));
        btnBaoTri.addActionListener(e -> filterByStatus(TinhTrangPhong.BAO_TRI));
        btnDaDat.addActionListener(e -> filterByStatus(TinhTrangPhong.DA_DAT));
    }

    private JButton createStatusButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(135, 38));
        return btn;
    }

    // Khởi tạo panel hiển thị danh sách phòng dạng lưới (grid)
    // Mỗi phòng được hiển thị dưới dạng một card (RoomCard)
    private void initRoomGrid() {
        // GridLayout 4 cột, nhưng mỗi ô sẽ chứa một wrapper panel
        roomGridPanel = new JPanel(new GridLayout(0, 4, 20, 20)); // 4 cột, khoảng cách 20
        roomGridPanel.setBackground(new Color(245, 247, 250));
        roomGridPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        JScrollPane scroll = new JScrollPane(roomGridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    private LocalDate getSelectedDate() {
        try {
            return LocalDate.parse(txtNgayXem.getText(), formatter);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    // Mở dialog chọn ngày (JDateChooser)
    // Chuyển đổi giữa Date và LocalDate
    // Sau khi chọn sẽ cập nhật lại danh sách phòng
    private void chonNgay() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");

        // === SỬA LỖI TẠI ĐÂY ===
        if (!txtNgayXem.getText().trim().isEmpty()) {
            try {
                // Parse chuỗi thành LocalDate
                LocalDate localDate = LocalDate.parse(txtNgayXem.getText().trim(), formatter);

                // Chuyển LocalDate → java.util.Date (đúng cách)
                java.util.Date utilDate = java.util.Date.from(
                        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                dateChooser.setDate(utilDate);
            } catch (Exception ignored) {
                // Nếu parse lỗi thì không set ngày
            }
        }

        Object[] message = { "Chọn ngày xem phòng:", dateChooser };

        int option = JOptionPane.showConfirmDialog(this, message,
                "Chọn ngày xem phòng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION && dateChooser.getDate() != null) {
            // Lấy ngày từ JDateChooser và chuyển về LocalDate
            LocalDate selectedDate = dateChooser.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            txtNgayXem.setText(selectedDate.format(formatter));
            filterRooms();
        }
    }

    // Hiển thị danh sách phòng lên giao diện:
    // - Nếu không có phòng → hiển thị thông báo
    // - Nếu có → tạo RoomCard cho từng phòng
    // - Click vào phòng sẽ mở RoomActionDialog
    private void renderRooms(List<PhongDTO> rooms, LocalDate selectedDate) {
        roomGridPanel.removeAll();

        if (rooms.isEmpty()) {
            JLabel lblEmpty = new JLabel("Không tìm thấy phòng phù hợp.", SwingConstants.CENTER);
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            roomGridPanel.add(lblEmpty);
        } else {
            for (PhongDTO p : rooms) {
                RoomCard card = new RoomCard(p);

                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        new RoomActionDialog(p, selectedDate, () -> loadData()).setVisible(true);
                    }
                });

                JPanel wrapper = new JPanel(new GridBagLayout());
                wrapper.setOpaque(false);
                wrapper.add(card);
                roomGridPanel.add(wrapper);
            }
        }

        roomGridPanel.revalidate();
        roomGridPanel.repaint();
    }

    // Load toàn bộ danh sách phòng từ Service
    // Sử dụng SwingWorker để tránh treo UI
    // Sau khi load xong sẽ gọi renderRooms()
    private void loadData(LocalDate selectedDate) {
        if (roomGridPanel == null)
            return;

        roomGridPanel.removeAll();
        JLabel lblLoading = new JLabel("Đang tải dữ liệu...", SwingConstants.CENTER);
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        roomGridPanel.add(lblLoading);
        roomGridPanel.revalidate();
        roomGridPanel.repaint();

        new SwingWorker<List<PhongDTO>, Void>() {
            @Override
            protected List<PhongDTO> doInBackground() {
                return phongService.timTatCaPhongDTO();
            }

            @Override
            protected void done() {
                try {
                    renderRooms(get(), selectedDate);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Lọc phòng theo trạng thái
    // Sử dụng SwingWorker để tránh treo UI
    private void filterByStatus(TinhTrangPhong status) {
        if (roomGridPanel == null)
            return;
        new SwingWorker<List<PhongDTO>, Void>() {
            @Override
            protected List<PhongDTO> doInBackground() {
                return phongService.timTatCaPhongDTO().stream()
                        .filter(p -> p.getTinhTrang() == status)
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    renderRooms(get(), getSelectedDate());
                } catch (Exception ex) {
                }
            }
        }.execute();
    }

    // Lọc phòng theo tầng và ngày
    // Sử dụng SwingWorker để tránh treo UI
    private void filterRooms() {
        if (roomGridPanel == null)
            return;
        int selectedIndex = cbTang.getSelectedIndex();
        LocalDate date = getSelectedDate();

        new SwingWorker<List<PhongDTO>, Void>() {
            @Override
            protected List<PhongDTO> doInBackground() {
                List<PhongDTO> rooms = phongService.timTatCaPhongDTO();
                if (selectedIndex > 0) { // Lọc theo tầng (1, 2, 3...)
                    String floorDigit = String.valueOf(selectedIndex);
                    rooms = rooms.stream().filter(p -> p.getMaPhong() != null &&
                            p.getMaPhong().length() >= 2 &&
                            String.valueOf(p.getMaPhong().charAt(1)).equals(floorDigit))
                            .collect(Collectors.toList());
                }
                return rooms;
            }

            @Override
            protected void done() {
                try {
                    renderRooms(get(), date);
                } catch (Exception ex) {
                }
            }
        }.execute();
    }

    @Override
    public void loadData() {
        loadData(getSelectedDate());
    }

    @Override
    protected void addNew() {
    }

    @Override
    protected void editSelected() {
    }

    @Override
    protected void deleteSelected() {
    }

    @Override
    protected void search() {
        filterRooms();
    }
}