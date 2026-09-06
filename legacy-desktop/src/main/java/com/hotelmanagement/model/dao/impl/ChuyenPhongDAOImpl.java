package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.ChuyenPhongDAO;

import com.hotelmanagement.model.entity.ChuyenPhong;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể ChuyenPhong.
 * Lưu trữ và truy xuất lịch sử chuyển đổi phòng của khách hàng.
 */
public class ChuyenPhongDAOImpl implements ChuyenPhongDAO {

    public ChuyenPhong findById(Long maChuyenPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(ChuyenPhong.class, maChuyenPhong);
        } finally {
            em.close();
        }
    }

    public List<ChuyenPhong> findByDatPhong(Long maDP) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<ChuyenPhong> query = em.createQuery(
                    "SELECT cp FROM ChuyenPhong cp WHERE cp.datPhong.maDP = :maDP ORDER BY cp.ngayChuyen DESC",
                    ChuyenPhong.class);
            query.setParameter("maDP", maDP);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<ChuyenPhong> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT cp FROM ChuyenPhong cp ORDER BY cp.ngayChuyen DESC", ChuyenPhong.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(ChuyenPhong chuyenPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(chuyenPhong);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu chuyển phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
