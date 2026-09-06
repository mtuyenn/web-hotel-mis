package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.ChiTietDichVuDAO;

import com.hotelmanagement.model.entity.ChiTietDichVu;
import com.hotelmanagement.model.keyclass.ChiTietDichVuId;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể ChiTietDichVu.
 * Xử lý việc thêm, xóa và truy vấn các dịch vụ được sử dụng trong một đặt phòng.
 */
public class ChiTietDichVuDAOImpl implements ChiTietDichVuDAO {

    public ChiTietDichVu findById(ChiTietDichVuId id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(ChiTietDichVu.class, id);
        } finally {
            em.close();
        }
    }

    public void save(ChiTietDichVu chiTiet) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(chiTiet);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi thêm chi tiết dịch vụ: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(ChiTietDichVuId id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ChiTietDichVu ct = em.find(ChiTietDichVu.class, id);
            if (ct != null) {
                em.remove(ct);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi xóa chi tiết dịch vụ: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // Nếu cần: xóa toàn bộ chi tiết dịch vụ của một đặt phòng
    public void deleteByDatPhong(Long maDP) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                    "DELETE FROM ChiTietDichVu c WHERE c.datPhong.maDP = :maDP").setParameter("maDP", maDP)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public java.util.List<ChiTietDichVu> findByMaDP(Long maDP) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM ChiTietDichVu c WHERE c.datPhong.maDP = :maDP", ChiTietDichVu.class)
                    .setParameter("maDP", maDP)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}