package com.hospitality.mis;



import org.junit.jupiter.api.Test;
import com.hospitality.mis.common.actor.ActorId;
import com.hospitality.mis.common.audit.AuditEntry;
import com.hospitality.mis.common.idempotency.IdempotencyKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ArchitectureTest {

    @Test

    void applicationAndSharedPrimitivesHaveOneRuntimeBoundary() {
        assertThat(HospitalityMisApplication.class.getPackage()).isEqualTo(ArchitectureTest.class.getPackage());
        assertThat(ActorId.class.getPackage()).isNotEqualTo(HospitalityMisApplication.class.getPackage());
        assertThat(AuditEntry.class.getPackage()).isNotEqualTo(HospitalityMisApplication.class.getPackage());
        assertThat(IdempotencyKey.class.getPackage()).isNotEqualTo(HospitalityMisApplication.class.getPackage());
    }

    @Test
    void sharedPrimitivesEnforceTheirCanonicalInvariants() {
        assertThat(ActorId.system().value()).isEqualTo(ActorId.SYSTEM_VALUE);
        assertThat(new IdempotencyKey(" request-1 ").value()).isEqualTo("request-1");
        assertThat(AuditEntry.now(new ActorId("frontdesk"), "GUEST_CREATED", "GUEST", "41",
                null, "created", null).occurredAt()).isNotNull();
        assertThatThrownBy(() -> new ActorId(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IdempotencyKey(" ")).isInstanceOf(IllegalArgumentException.class);
    }
}
