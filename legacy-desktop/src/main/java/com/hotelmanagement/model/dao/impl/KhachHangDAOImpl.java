package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.KhachHangDAO;

import com.hotelmanagement.model.entity.KhachHang;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể KhachHang.
 * Quản lý thông tin khách hàng như lưu, cập nhật, xóa và tìm kiếm theo CCCD/Mã.
 */
public class KhachHangDAOImpl implements KhachHangDAO {

    public KhachHang findByMakh(Long maKH) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(KhachHang.class, maKH);
        } finally {
            em.close();
        }
    }

    public KhachHang findByCccd(String cccd) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<KhachHang> query = em.createQuery(
                    "SELECT k FROM KhachHang k WHERE k.cccd = :cccd", KhachHang.class);
            query.setParameter("cccd", cccd);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public List<KhachHang> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT k FROM KhachHang k ORDER BY k.tenKH", KhachHang.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(KhachHang kh) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (kh.getMaKH() == null || findByMakh(kh.getMaKH()) == null) {
                em.persist(kh);
            } else {
                em.merge(kh);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi lưu khách hàng", e);
        } finally {
            em.close();
        }
    }

    public void delete(String cccd) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TypedQuery<KhachHang> query = em.createQuery(
                    "SELECT k FROM KhachHang k WHERE k.cccd = :cccd", KhachHang.class);
            query.setParameter("cccd", cccd);
            KhachHang kh = query.getResultStream().findFirst().orElse(null);
            if (kh != null)
                em.remove(kh);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Không thể xóa khách hàng vì có đặt phòng đang hoạt động", e);
        } finally {
            em.close();
        }
    }
}