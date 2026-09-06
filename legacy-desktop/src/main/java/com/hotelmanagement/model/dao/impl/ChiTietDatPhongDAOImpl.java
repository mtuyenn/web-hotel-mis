package com.hotelmanagement.model.dao.impl;

import com.hotelmanagement.model.dao.ChiTietDatPhongDAO;
import com.hotelmanagement.model.entity.ChiTietDatPhong;
import com.hotelmanagement.model.entity.DatPhong;
import com.hotelmanagement.model.entity.Phong;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.keyclass.ChiTietDatPhongId;
import com.hotelmanagement.model.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp triển khai các thao tác truy cập dữ liệu cho thực thể ChiTietDatPhong.
 * Xử lý các logic liên quan đến phòng được đặt trong một đơn đặt phòng.
 */
public class ChiTietDatPhongDAOImpl implements ChiTietDatPhongDAO {

    public DatPhong findByMaDPWithFullDetails(Long maDP) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<DatPhong> query = em.createQuery(
                    """
                            SELECT DISTINCT dp
                            FROM DatPhong dp
                            LEFT JOIN FETCH dp.khachHang
                            LEFT JOIN FETCH dp.nhanVien
                            LEFT JOIN FETCH dp.chiTietDatPhongs ctp
                            LEFT JOIN FETCH ctp.phong p
                            LEFT JOIN FETCH p.loaiPhong lp
                            LEFT JOIN FETCH dp.chiTietDichVus ctdv
                            LEFT JOIN FETCH ctdv.dichVu
                            WHERE dp.maDP = :maDP
                            """,
                    DatPhong.class);
            query.setParameter("maDP", maDP);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<ChiTietDatPhong> findByPhong(String maPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<ChiTietDatPhong> query = em.createQuery(
                    "SELECT c FROM ChiTietDatPhong c WHERE c.phong.maPhong = :maPhong",
                    ChiTietDatPhong.class);
            query.setParameter("maPhong", maPhong);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public ChiTietDatPhong findById(ChiTietDatPhongId id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(ChiTietDatPhong.class, id);
        } finally {
            em.close();
        }
    }

    public ChiTietDatPhong findByMaDPAndMaPhong(Long maDP, String maPhong) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<ChiTietDatPhong> query = em.createQuery(
                    "SELECT c FROM ChiTietDatPhong c " +
                            "WHERE c.datPhong.maDP = :maDP AND c.phong.maPhong = :maPhong",
                    ChiTietDatPhong.class);
            query.setParameter("maDP", maDP);
            query.setParameter("maPhong", maPhong);

            List<ChiTietDatPhong> result = query.getResultList();
            return result.isEmpty() ? null : result.get(0);

        } finally {
            em.close();
        }
    }

    public void save(ChiTietDatPhong ctp) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(ctp);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu chi tiết đặt phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void update(ChiTietDatPhong ctp) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(ctp);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi update chi tiết đặt phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void updateTrangThaiByMaDPAndMaPhong(Long maDP, String maPhong, TinhTrangPhong trangThai) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ChiTietDatPhongId id = new ChiTietDatPhongId(maDP, maPhong);
            ChiTietDatPhong ctp = em.find(ChiTietDatPhong.class, id);
            if (ctp != null) {
                ctp.setTrangThai(trangThai);
                em.merge(ctp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi cập nhật trạng thái chi tiết đặt phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(ChiTietDatPhongId id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ChiTietDatPhong ctp = em.find(ChiTietDatPhong.class, id);
            if (ctp != null)
                em.remove(ctp);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateThoiGian(Long maDP, String maPhong,
            LocalDateTime ngayNhan, LocalDateTime ngayTra) {

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ChiTietDatPhongId id = new ChiTietDatPhongId(maDP, maPhong);
            ChiTietDatPhong ctp = em.find(ChiTietDatPhong.class, id);

            if (ctp == null) {
                throw new RuntimeException("Không tìm thấy chi tiết đặt phòng");
            }

            ctp.setNgayNhan(ngayNhan);
            ctp.setNgayTra(ngayTra);

            em.merge(ctp);

            tx.commit();

        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateTrangThaiByMaDP(Long maDP, TinhTrangPhong trangThai) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            DatPhong dp = em.find(DatPhong.class, maDP);
            if (dp != null && dp.getChiTietDatPhongs() != null) {
                for (ChiTietDatPhong ctp : dp.getChiTietDatPhongs()) {
                    ctp.setTrangThai(trangThai);
                    em.merge(ctp);
                }
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

    public void updatePhongMoi(Long maDP, String maPhongCu, String maPhongMoi) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. Tìm chi tiết đặt phòng cũ
            ChiTietDatPhongId oldId = new ChiTietDatPhongId(maDP, maPhongCu);
            ChiTietDatPhong oldCtp = em.find(ChiTietDatPhong.class, oldId);

            if (oldCtp == null) {
                throw new RuntimeException("Không tìm thấy chi tiết đặt phòng cũ");
            }

            // 2. Lấy phòng mới
            Phong phongMoi = em.find(Phong.class, maPhongMoi);
            if (phongMoi == null) {
                throw new RuntimeException("Phòng mới không tồn tại");
            }

            // 3. Tạo chi tiết mới
            ChiTietDatPhong newCtp = new ChiTietDatPhong();
            newCtp.setDatPhong(oldCtp.getDatPhong());
            newCtp.setPhong(phongMoi);
            newCtp.setNgayNhan(oldCtp.getNgayNhan());
            newCtp.setNgayTra(oldCtp.getNgayTra());

            em.persist(newCtp);
            em.remove(oldCtp);

            tx.commit();

        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
