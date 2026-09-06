package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.BaoTriDAO;

import com.hotelmanagement.model.entity.BaoTri;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu (CRUD) cho thực thể BaoTri.
 * Quản lý các hoạt động liên quan đến bảo trì phòng.
 */
public class BaoTriDAOImpl implements BaoTriDAO {

    public BaoTri findById(String maBT) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(BaoTri.class, maBT);
        } finally {
            em.close();
        }
    }

    public List<BaoTri> findByPhong(String maPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<BaoTri> query = em.createQuery(
                    "SELECT bt FROM BaoTri bt WHERE bt.phong.maPhong = :maPhong ORDER BY bt.ngayBaoTri DESC",
                    BaoTri.class);
            query.setParameter("maPhong", maPhong);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<BaoTri> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT bt FROM BaoTri bt ORDER BY bt.ngayBaoTri DESC", BaoTri.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<String> layDanhSachLoaiBaoTri() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT bt.loaiBaoTri FROM BaoTri bt WHERE bt.loaiBaoTri IS NOT NULL", String.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(BaoTri baoTri) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(baoTri);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu bảo trì: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void update(BaoTri baoTri) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(baoTri);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi cập nhật bảo trì: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
