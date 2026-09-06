package com.hotelmanagement.view.khachhang;

import com.hotelmanagement.controller.KhachHangController;
import com.hotelmanagement.model.dto.KhachHangDTO;
import com.hotelmanagement.view.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Lớp KhachHangView - Màn hình quản lý khách hàng
 * 
 * Chức năng:
 * - Hiển thị danh sách tất cả khách hàng
 * - Tìm kiếm khách hàng theo CCCD, tên hoặc số điện thoại
 * - Thêm khách hàng mới
 * - Sửa thông tin khách hàng
 * - Xóa khách hàng
 * - Tự động tải dữ liệu từ cơ sở dữ liệu
 * 
 * Thiết kế:
 * - Kế thừa từ BaseView để sử dụng chung các thành phần (thanh tìm kiếm, nút
 * chức năng)
 * - Sử dụng JTable để hiển thị danh sách khách hàng
 * - Sử dụng SwingWorker để tải dữ liệu bất đồng bộ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class KhachHangView extends BaseView {

    private final KhachHangController controller = new KhachHangController();
    private List<KhachHangDTO> currentList;

    public KhachHangView() {
        super();
        setName("Khách Hàng");

        // Khởi tạo bảng
        String[] columns = { "Mã KH", "CCCD", "Tên Khách Hàng", "SĐT", "Email", "Địa chỉ" };
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

        // Sự kiện các nút chức năng
        btnAdd.addActionListener(e -> addNew());
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData());
        btnSearch.addActionListener(e -> search());

        loadData();
    }

    @Override
    protected void loadData() {
        new SwingWorker<List<KhachHangDTO>, Void>() {
            @Override
            protected List<KhachHangDTO> doInBackground() {
                return controller.layTatCaKhachHang();
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    model.setRowCount(0);
                    for (KhachHangDTO kh : currentList) {
                        model.addRow(new Object[] {
                                kh.getMaKH(), kh.getCccd(), kh.getTenKH(),
                                kh.getSoDienThoai(), kh.getEmail(), kh.getDiaChi()
                        });
                    }
                } catch (Exception ex) {
                    showError("Lỗi tải dữ liệu khách hàng: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    protected void addNew() {
        KhachHangDialog dialog = new KhachHangDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Khách Hàng",
                null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadData();
        }
    }

    @Override
    protected void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Vui lòng chọn một khách hàng để sửa");
            return;
        }
        String cccd = (String) model.getValueAt(row, 1);
        KhachHangDTO dto = controller.timKhachHangTheoCCCD(cccd);
        if (dto != null) {
            KhachHangDialog dialog = new KhachHangDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Cập Nhật Khách Hàng", dto);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadData();
            }
        }
    }

    @Override
    protected void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Vui lòng chọn một khách hàng để xóa");
            return;
        }
        String cccd = (String) model.getValueAt(row, 1);
        String name = (String) model.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng " + name + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.xoaKhachHang(cccd);
                showSuccess("Xóa thành công!");
                loadData();
            } catch (Exception ex) {
                showError("Lỗi xóa khách hàng: " + ex.getMessage());
            }
        }
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
            for (KhachHangDTO kh : currentList) {
                if ((kh.getTenKH() != null && kh.getTenKH().toLowerCase().contains(query)) ||
                        (kh.getCccd() != null && kh.getCccd().contains(query)) ||
                        (kh.getSoDienThoai() != null && kh.getSoDienThoai().contains(query))) {
                    model.addRow(new Object[] {
                            kh.getMaKH(), kh.getCccd(), kh.getTenKH(),
                            kh.getSoDienThoai(), kh.getEmail(), kh.getDiaChi()
                    });
                }
            }
        }
    }
}