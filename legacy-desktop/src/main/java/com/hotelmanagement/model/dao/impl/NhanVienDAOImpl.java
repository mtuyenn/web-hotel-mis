package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.NhanVienDAO;

import com.hotelmanagement.model.entity.NhanVien;
import com.hotelmanagement.model.enums.ChucVuNhanVien;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể NhanVien.
 * Xử lý các logic quản lý nhân sự, tìm kiếm nhân viên, và phân quyền cơ bản.
 */
public class NhanVienDAOImpl implements NhanVienDAO {

    public NhanVien findByMaNV(String maNV) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(NhanVien.class, maNV);
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<NhanVien> query = em.createQuery(
                    "SELECT n FROM NhanVien n ORDER BY n.tenNV", NhanVien.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(NhanVien nv) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (nv.getMaNV() == null || findByMaNV(nv.getMaNV()) == null) {
                em.persist(nv);
            } else {
                em.merge(nv);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi lưu nhân viên", e);
        } finally {
            em.close();
        }
    }

    public void delete(String maNV) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            NhanVien nv = em.find(NhanVien.class, maNV);
            if (nv != null)
                em.remove(nv);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Không thể xóa nhân viên !!", e);
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findByChucVu(ChucVuNhanVien chucVu) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT nv FROM NhanVien nv WHERE nv.chucVu = :chucVu";
            return em.createQuery(jpql, NhanVien.class)
                    .setParameter("chucVu", chucVu)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm nhân viên theo tên (tenNV) - dùng cho đăng nhập
     */
    public NhanVien findByTenNV(String tenNV) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<NhanVien> query = em.createQuery(
                    "SELECT n FROM NhanVien n WHERE LOWER(n.tenNV) = LOWER(:tenNV) OR n.maNV = :tenNV",
                    NhanVien.class);
            query.setParameter("tenNV", tenNV);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

}
