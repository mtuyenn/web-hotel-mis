package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.DichVuDAO;

import com.hotelmanagement.model.entity.DichVu;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể DichVu.
 * Cung cấp các chức năng quản lý danh mục dịch vụ của khách sạn.
 */
public class DichVuDAOImpl implements DichVuDAO {
    public DichVu findByMaDV(String maDV) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(DichVu.class, maDV);
        } finally {
            em.close();
        }
    }

    public List<DichVu> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<DichVu> query = em.createQuery(
                    "SELECT d FROM DichVu d ORDER BY d.tenDichVu", DichVu.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(DichVu dichVu) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (dichVu.getMaDV() == null || findByMaDV(dichVu.getMaDV()) == null) {
                em.persist(dichVu);
            } else {
                em.merge(dichVu);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu dịch vụ: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(String maDV) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DichVu dv = em.find(DichVu.class, maDV);
            if (dv != null) {
                em.remove(dv);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Không thể xóa dịch vụ (có thể đang được sử dụng)", e);
        } finally {
            em.close();
        }
    }
}
