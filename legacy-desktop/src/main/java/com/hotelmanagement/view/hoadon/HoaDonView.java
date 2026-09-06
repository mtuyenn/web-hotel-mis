package com.hotelmanagement.view.hoadon;

import com.hotelmanagement.controller.HoaDonController;
import com.hotelmanagement.model.dto.HoaDonDTO;
import com.hotelmanagement.view.AppIcon;
import com.hotelmanagement.view.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lớp HoaDonView - Màn hình quản lý hóa đơn
 * 
 * Chức năng:
 * - Hiển thị danh sách tất cả hóa đơn
 * - Tìm kiếm hóa đơn theo mã hoặc tên khách hàng
 * - Xem chi tiết hóa đơn
 * - Tự động tải dữ liệu từ cơ sở dữ liệu
 * 
 * Thiết kế:
 * - Kế thừa từ BaseView để sử dụng chung các thành phần (thanh tìm kiếm, nút
 * chức năng)
 * - Sử dụng JTable để hiển thị danh sách hóa đơn
 * - Sử dụng SwingWorker để tải dữ liệu bất đồng bộ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class HoaDonView extends BaseView {

    private final HoaDonController controller = new HoaDonController();
    private List<HoaDonDTO> currentList;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HoaDonView() {
        super();
        setName("Quản lý hóa đơn");

        // Custom lại các nút từ BaseView (Hóa đơn cứng trong DB, không thêm/xóa tự do)
        btnAdd.setVisible(false);
        btnDelete.setVisible(false);
        btnEdit.setText("Xem Hóa Đơn");
        btnEdit.setIcon(AppIcon.of(AppIcon.Type.EYE_OPEN, Color.BLACK, 16));
        btnEdit.setPreferredSize(new Dimension(160, 38));

        searchPlaceholder = "Nhập Tên Khách Hàng hoặc Mã HĐ...";

        // Khởi tạo bảng
        String[] columns = { "Mã HD", "Khách Hàng", "Mã Đặt", "Ngày Xuất", "Tổng Tiền", "Phương Thức", "Trạng Thái" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Khởi tạo sự kiện cho các nút (Fix lỗi không ấn được nút)
        btnEdit.addActionListener(e -> editSelected());
        btnRefresh.addActionListener(e -> loadData());
        btnSearch.addActionListener(e -> search());

        // Tự động tải lại dữ liệu khi tab được mở (để thấy hóa đơn mới sinh từ nhận
        // phòng)
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataFresh();
            }
        });

        loadDataFresh();
    }

    /**
     * Load dữ liệu mới nhất (Fresh) - Đây là phương thức chính chúng ta dùng
     */
    private void loadDataFresh() {
        new SwingWorker<List<HoaDonDTO>, Void>() {
            @Override
            protected List<HoaDonDTO> doInBackground() {
                return controller.getAllHoaDonFresh(); // Gọi method fresh
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    model.setRowCount(0);

                    for (HoaDonDTO hd : currentList) {
                        String ngayXuat = (hd.getNgayXuatHD() != null)
                                ? hd.getNgayXuatHD().format(formatter)
                                : "";

                        String tongTien = (hd.getTongTienPhaiTra() != null)
                                ? String.format("%,d ₫", hd.getTongTienPhaiTra().longValue())
                                : "0 ₫";

                        String trangThai = hd.getTrangThai() != null ? hd.getTrangThai().name() : "";
                        String phuongThuc = hd.getPhuongThucThanhToan() != null
                                ? hd.getPhuongThucThanhToan().name()
                                : "";

                        model.addRow(new Object[] {
                                hd.getMaHoaDon(),
                                hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách Vãng Lai",
                                hd.getMaDP(),
                                ngayXuat,
                                tongTien,
                                phuongThuc,
                                trangThai
                        });
                    }
                } catch (Exception ex) {
                    showError("Lỗi tải danh sách hóa đơn: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    @Override
    protected void addNew() {
        // Trống vì ẩn nút
    }

    @Override
    protected void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Vui lòng chọn một hóa đơn để xem trên bảng!");
            return;
        }
        Long maHD = (Long) model.getValueAt(row, 0);
        if (currentList != null) {
            HoaDonDTO selectedHD = currentList.stream()
                    .filter(hd -> hd.getMaHoaDon().equals(maHD))
                    .findFirst()
                    .orElse(null);
            if (selectedHD != null) {
                ChiTietHoaDonDialog dialog = new ChiTietHoaDonDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                        selectedHD);
                dialog.setVisible(true);
            }
        }
    }

    // Giữ lại loadData() cũ để tương thích với BaseView
    @Override
    protected void loadData() {
        loadDataFresh();
    }

    @Override
    protected void deleteSelected() {
        // Trống vì ẩn nút
    }

    @Override
    protected void search() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty() && currentList != null) {
            loadData();
            return;
        }

        if (currentList != null) {
            model.setRowCount(0);
            for (HoaDonDTO hd : currentList) {
                String tenKH = hd.getTenKhachHang() != null ? hd.getTenKhachHang().toLowerCase() : "";
                String maHDStr = hd.getMaHoaDon() != null ? hd.getMaHoaDon().toString() : "";

                if (tenKH.contains(query) || maHDStr.contains(query)) {
                    String dateStr = (hd.getNgayXuatHD() != null) ? hd.getNgayXuatHD().format(formatter) : "";
                    String tongTien = (hd.getTongTienPhaiTra() != null)
                            ? String.format("%,d VND", hd.getTongTienPhaiTra().longValue())
                            : "0";
                    String tt = (hd.getTrangThai() != null) ? hd.getTrangThai().name() : "";
                    String pt = (hd.getPhuongThucThanhToan() != null) ? hd.getPhuongThucThanhToan().name() : "";

                    model.addRow(new Object[] {
                            hd.getMaHoaDon(),
                            hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách Vãng Lai",
                            hd.getMaDP(),
                            dateStr,
                            tongTien,
                            pt,
                            tt
                    });
                }
            }
        }
    }

}