package com.hotelmanagement.view.nhanvien;

import com.hotelmanagement.controller.NhanVienController;
import com.hotelmanagement.model.dto.NhanVienDTO;
import com.hotelmanagement.view.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Lớp NhanVienView - Màn hình quản lý nhân viên
 * 
 * Chức năng:
 * - Hiển thị danh sách tất cả nhân viên
 * - Tìm kiếm nhân viên theo mã, tên hoặc chức vụ
 * - Thêm nhân viên mới
 * - Sửa thông tin nhân viên
 * - Xóa nhân viên
 * - Tự động tải dữ liệu từ cơ sở dữ liệu
 * 
 * Thiết kế:
 * - Kế thừa từ BaseView để sử dụng chung các thành phần (thanh tìm kiếm, nút
 * chức năng)
 * - Sử dụng JTable để hiển thị danh sách nhân viên
 * - Sử dụng SwingWorker để tải dữ liệu bất đồng bộ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class NhanVienView extends BaseView {

    private final NhanVienController controller = new NhanVienController();
    private List<NhanVienDTO> currentList;

    public NhanVienView() {
        super();
        this.searchPlaceholder = "Nhập mã, tên hoặc chức vụ để tìm...";
        setName("Quản lý nhân viên");

        // Khởi tạo bảng
        String[] columns = { "Mã NV", "Họ Tên", "Chức Vụ", "Số Điện Thoại", "Địa Chỉ" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(35);
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
        new SwingWorker<List<NhanVienDTO>, Void>() {
            @Override
            protected List<NhanVienDTO> doInBackground() {
                return controller.layTatCaNhanVien();
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    model.setRowCount(0);
                    for (NhanVienDTO nv : currentList) {
                        model.addRow(new Object[] {
                                nv.getMaNV(), nv.getTenNV(),
                                nv.getChucVu(), nv.getSoDienThoai(), nv.getDiaChi()
                        });
                    }
                } catch (Exception ex) {
                    showError("Lỗi tải dữ liệu nhân viên: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    protected void addNew() {
        NhanVienDialog dialog = new NhanVienDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Nhân Viên",
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
            showError("Vui lòng chọn một nhân viên để sửa");
            return;
        }
        String maNV = (String) model.getValueAt(row, 0);
        NhanVienDTO dto = controller.timNhanVien(maNV);
        if (dto != null) {
            NhanVienDialog dialog = new NhanVienDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Cập Nhật Nhân Viên", dto);
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
            showError("Vui lòng chọn một nhân viên để xóa");
            return;
        }
        String maNV = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhân viên " + name + " (" + maNV + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.xoaNhanVien(maNV);
                showSuccess("Xóa nhân viên thành công!");
                loadData();
            } catch (Exception ex) {
                showError("Lỗi xóa nhân viên: " + ex.getMessage());
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
            for (NhanVienDTO nv : currentList) {
                if ((nv.getTenNV() != null && nv.getTenNV().toLowerCase().contains(query)) ||
                        (nv.getMaNV() != null && nv.getMaNV().toLowerCase().contains(query)) ||
                        (nv.getSoDienThoai() != null && nv.getSoDienThoai().contains(query)) ||
                        (nv.getChucVu() != null && nv.getChucVu().toLowerCase().contains(query))) {
                    model.addRow(new Object[] {
                            nv.getMaNV(), nv.getTenNV(),
                            nv.getChucVu(), nv.getSoDienThoai(), nv.getDiaChi()
                    });
                }
            }
        }
    }
}
