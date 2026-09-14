package com.hospitality.mis.middleware.security;
import com.hospitality.mis.entity.governance.ApprovalRequest;




import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;



/**

 * Ngăn người yêu cầu tự phê duyệt yêu cầu đang chờ xử lý của mình ở lớp

 * HTTP. Dịch vụ phê duyệt vẫn chịu trách nhiệm chuyển đổi trạng thái.

 */

@Component("approvalAuthorization")

public class ApprovalAuthorization {

    /** EntityManager chỉ đọc requester của approval để chặn tự phê duyệt tại biên authorization. */
    private final EntityManager entityManager;



    /** Nhận dependency truy vấn; chuyển trạng thái approval vẫn thuộc service nghiệp vụ. */
    public ApprovalAuthorization(EntityManager entityManager) {

        this.entityManager = entityManager;

    }



    /**
     * Chỉ cho phép approver hợp lệ khác requester của bản ghi còn tồn tại.
     * Input thiếu hoặc approval không tồn tại đều trả false theo nguyên tắc fail-closed.
     */
    public boolean canApprove(Long approvalId, String approver) {

        if (approvalId == null || approver == null || approver.isBlank()) {

            return false;

        }

        try {

            // Chỉ đọc requester từ persistence; không tin giá trị requester do client gửi lên.
            String requester = entityManager.createQuery(

                            "select approval.requester from ApprovalRequest approval where approval.id = :id",

                            String.class)

                    .setParameter("id", approvalId)

                    .getSingleResult();

            return !approver.equals(requester);

        } catch (jakarta.persistence.NoResultException exception) {

            return false;

        }

    }

}
