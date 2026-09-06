package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.HoaDonDAO;

import com.hotelmanagement.model.entity.HoaDon;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể HoaDon.
 * Xử lý việc tạo mới, cập nhật và tra cứu hóa đơn thanh toán của khách.
 */
public class HoaDonDAOImpl implements HoaDonDAO {

    public HoaDon findByMaDP(Long maDP) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<HoaDon> query = em.createQuery(
                    "SELECT h FROM HoaDon h " +
                            "JOIN FETCH h.datPhong dp " +
                            "LEFT JOIN FETCH dp.khachHang KH " +
                            "LEFT JOIN FETCH dp.chiTietDatPhongs ctp " +
                            "LEFT JOIN FETCH ctp.phong p " +
                            "LEFT JOIN FETCH p.loaiPhong lp " +
                            "WHERE dp.maDP = :maDP",
                    HoaDon.class);
            query.setParameter("maDP", maDP);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public void save(HoaDon hd) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(hd);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(HoaDon hd) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(hd);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<HoaDon> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT hd FROM HoaDon hd " +
                            "JOIN FETCH hd.datPhong dp " +
                            "LEFT JOIN FETCH dp.khachHang " +
                            "LEFT JOIN FETCH dp.chiTietDatPhongs ctp " +
                            "LEFT JOIN FETCH ctp.phong p " +
                            "LEFT JOIN FETCH p.loaiPhong",
                    HoaDon.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<HoaDon> findAllFresh() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.clear();
            return em.createQuery(
                    "SELECT hd FROM HoaDon hd " +
                            "JOIN FETCH hd.datPhong dp " +
                            "LEFT JOIN FETCH dp.khachHang " +
                            "LEFT JOIN FETCH dp.chiTietDatPhongs ctp " +
                            "LEFT JOIN FETCH ctp.phong p " +
                            "LEFT JOIN FETCH p.loaiPhong " +
                            "ORDER BY hd.ngayXuatHD DESC",
                    HoaDon.class).getResultList();
        } finally {
            em.close();
        }
    }

    // kiểm tra nhân viên nào đang xử lý hóa đơn
    public boolean existsByNhanVien(String maNV) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(h) FROM HoaDon h WHERE h.datPhong.nhanVien.maNV = :maNV",
                    Long.class)
                    .setParameter("maNV", maNV)
                    .getSingleResult();

            return count > 0;
        } finally {
            em.close();
        }
    }
}