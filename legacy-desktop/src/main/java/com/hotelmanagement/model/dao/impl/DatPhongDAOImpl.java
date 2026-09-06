package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.DatPhongDAO;

import com.hotelmanagement.model.entity.DatPhong;
import com.hotelmanagement.model.enums.TinhTrangThanhToan;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể DatPhong.
 * Xử lý các nghiệp vụ lưu trữ, cập nhật, xóa và truy vấn đơn đặt phòng.
 */
public class DatPhongDAOImpl implements DatPhongDAO {

    /**
     * Lấy tất cả đặt phòng kèm đầy đủ thông tin (khách hàng, nhân viên, chi tiết
     * phòng, loại phòng)
     */
    public List<DatPhong> findAllWithDetails() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("""
                    SELECT DISTINCT dp
                    FROM DatPhong dp
                    LEFT JOIN FETCH dp.khachHang
                    LEFT JOIN FETCH dp.nhanVien
                    LEFT JOIN FETCH dp.chiTietDatPhongs ctp
                    LEFT JOIN FETCH ctp.phong p
                    LEFT JOIN FETCH p.loaiPhong
                    ORDER BY dp.ngayDat DESC
                    """, DatPhong.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public DatPhong findByMaDP(Long maDP) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(DatPhong.class, maDP);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả đặt phòng chưa thanh toán 
     */
    public List<DatPhong> findAllChuaThanhToan() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = """
                    SELECT DISTINCT d FROM DatPhong d
                    LEFT JOIN FETCH d.khachHang
                    LEFT JOIN FETCH d.nhanVien
                    LEFT JOIN FETCH d.chiTietDatPhongs ctp
                    LEFT JOIN FETCH ctp.phong p
                    LEFT JOIN FETCH p.loaiPhong
                    LEFT JOIN d.hoaDon hd
                    WHERE hd IS NULL
                       OR hd.trangThai = :trangThai
                    """;
            TypedQuery<DatPhong> query = em.createQuery(jpql, DatPhong.class);
            query.setParameter("trangThai", TinhTrangThanhToan.CHUA_THANH_TOAN);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<DatPhong> findByKhachHang(Long maKH) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<DatPhong> query = em.createQuery(
                    "SELECT DISTINCT d FROM DatPhong d LEFT JOIN FETCH d.khachHang LEFT JOIN FETCH d.nhanVien LEFT JOIN FETCH d.chiTietDatPhongs ctp LEFT JOIN FETCH ctp.phong p LEFT JOIN FETCH p.loaiPhong WHERE d.khachHang.maKH = :maKH ORDER BY d.ngayDat DESC",
                    DatPhong.class);
            query.setParameter("maKH", maKH);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(DatPhong dp) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(dp);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu đặt phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void update(DatPhong dp) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(dp);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi cập nhật đặt phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(Long maDP) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DatPhong dp = em.find(DatPhong.class, maDP);
            if (dp != null) {
                em.remove(dp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi xóa đặt phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // Các method thống kê
    public List<DatPhong> findByNgayDatBetween(LocalDateTime from, LocalDateTime to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT dp FROM DatPhong dp WHERE dp.ngayDat BETWEEN :from AND :to";
            TypedQuery<DatPhong> query = em.createQuery(jpql, DatPhong.class);
            query.setParameter("from", from);
            query.setParameter("to", to);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}