package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.LoaiPhongDAO;

import com.hotelmanagement.model.entity.LoaiPhong;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể LoaiPhong.
 * Cung cấp các phương thức quản lý danh mục loại phòng khách sạn.
 */
public class LoaiPhongDAOImpl implements LoaiPhongDAO {

    public LoaiPhong findByMaLoaiPhong(String maLoaiPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(LoaiPhong.class, maLoaiPhong);
        } finally {
            em.close();
        }
    }

    public List<LoaiPhong> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<LoaiPhong> query = em.createQuery(
                    "SELECT lp FROM LoaiPhong lp ORDER BY lp.tenLoai", LoaiPhong.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(LoaiPhong loaiPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (loaiPhong.getMaLoaiPhong() == null || findByMaLoaiPhong(loaiPhong.getMaLoaiPhong()) == null) {
                em.persist(loaiPhong);
            } else {
                em.merge(loaiPhong);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu loại phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(String maLoaiPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LoaiPhong lp = em.find(LoaiPhong.class, maLoaiPhong);
            if (lp != null) {
                em.remove(lp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Không thể xóa loại phòng (có phòng đang sử dụng)", e);
        } finally {
            em.close();
        }
    }
}