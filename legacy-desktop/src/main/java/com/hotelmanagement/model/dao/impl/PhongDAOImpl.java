package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.PhongDAO;

import com.hotelmanagement.model.entity.LoaiPhong;
import com.hotelmanagement.model.entity.Phong;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể Phong.
 * Xử lý các nghiệp vụ truy vấn phòng trống, cập nhật trạng thái phòng.
 */
public class PhongDAOImpl implements PhongDAO {

    public PhongDAOImpl() {
    }

    public Phong findByMaPhong(String maPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Sử dụng JOIN FETCH để load luôn LoaiPhong, tránh lazy proxy error
            TypedQuery<Phong> query = em.createQuery(
                    "SELECT p FROM Phong p " +
                            "LEFT JOIN FETCH p.loaiPhong " +
                            "WHERE p.maPhong = :maPhong",
                    Phong.class);
            query.setParameter("maPhong", maPhong);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public List<Phong> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Phong p JOIN FETCH p.loaiPhong", Phong.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm phòng trống theo khoảng thời gian và loại phòng (JPQL phức tạp)
     */
    public List<Phong> timPhongTrong(LocalDateTime ngayNhan, LocalDateTime ngayTra, LoaiPhong loaiPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = """
                    SELECT p FROM Phong p
                    JOIN FETCH p.loaiPhong
                    WHERE p.tinhTrang IN :trangThai
                      AND NOT EXISTS (
                          SELECT ctp FROM ChiTietDatPhong ctp
                          WHERE ctp.phong.maPhong = p.maPhong
                            AND ctp.ngayNhan < :ngayTra
                            AND ctp.ngayTra > :ngayNhan
                      )
                    """;

            if (loaiPhong != null) {
                jpql += " AND p.loaiPhong = :loaiPhong";
            }

            TypedQuery<Phong> query = em.createQuery(jpql, Phong.class);

            if (loaiPhong != null) {
                query.setParameter("loaiPhong", loaiPhong);
            }
            query.setParameter("trangThai", List.of(TinhTrangPhong.SAN_SANG, TinhTrangPhong.DANG_DON_DEP));
            query.setParameter("ngayNhan", ngayNhan);
            query.setParameter("ngayTra", ngayTra);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void updateTinhTrang(String maPhong, TinhTrangPhong tinhTrang) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Phong p = em.find(Phong.class, maPhong);
            if (p != null) {
                p.setTinhTrang(tinhTrang);
                em.merge(p);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void save(Phong phong) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(phong);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Phong> timBoiTinhTrang(TinhTrangPhong tinhTrangPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT p FROM Phong p WHERE p.tinhTrang = :tinhTrang";

            TypedQuery<Phong> query = em.createQuery(jpql, Phong.class);
            query.setParameter("tinhTrang", tinhTrangPhong);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Phong> timPhongDangO() {
        return timBoiTinhTrang(TinhTrangPhong.DANG_O);
    }

    public List<Phong> timPhongDangDonDep() {
        return timBoiTinhTrang(TinhTrangPhong.DANG_DON_DEP);
    }

    public List<Phong> timPhongBaoTri() {
        return timBoiTinhTrang(TinhTrangPhong.BAO_TRI);
    }
}