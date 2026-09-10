package com.hospitality.mis.middleware.security;
import com.hospitality.mis.entity.governance.ApprovalRequest;




import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;



/**

 * Keeps the requester from approving its own pending approval at the HTTP

 * boundary. The approval service remains the owner of the state transition.

 */

@Component("approvalAuthorization")

public class ApprovalAuthorization {

    private final EntityManager entityManager;



    public ApprovalAuthorization(EntityManager entityManager) {

        this.entityManager = entityManager;

    }



    public boolean canApprove(Long approvalId, String approver) {

        if (approvalId == null || approver == null || approver.isBlank()) {

            return false;

        }

        try {

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
