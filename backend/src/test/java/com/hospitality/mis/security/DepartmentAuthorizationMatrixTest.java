package com.hospitality.mis.security;

import com.hospitality.mis.service.auth.AuthService;
import com.hospitality.mis.service.auth.CustomerAccountService;
import com.hospitality.mis.service.billing.BillingService;
import com.hospitality.mis.service.billing.PaymentTransactionService;
import com.hospitality.mis.service.billing.ReceiptService;
import com.hospitality.mis.service.billing.ServiceCatalogService;
import com.hospitality.mis.service.finance.FinanceService;
import com.hospitality.mis.service.governance.ApprovalService;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.guest.GuestService;
import com.hospitality.mis.service.guest.MembershipHistoryService;
import com.hospitality.mis.service.operations.EquipmentIncidentService;
import com.hospitality.mis.service.operations.InventoryMovementService;
import com.hospitality.mis.service.operations.MaintenanceService;
import com.hospitality.mis.service.operations.RoomTransferService;
import com.hospitality.mis.service.reservation.ReservationService;
import com.hospitality.mis.service.room.RoomService;
import com.hospitality.mis.service.room.RoomEquipmentService;
import com.hospitality.mis.middleware.security.ApprovalAuthorization;
import com.hospitality.mis.dto.reservation.ReservationDtos;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.*;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/** Independent HTTP allow/deny matrix; mocked services isolate endpoint RBAC from business rules. */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:departmentmatrix;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class DepartmentAuthorizationMatrixTest {
    @Autowired MockMvc mvc;
    @Autowired RequestMappingHandlerMapping requestMappingHandlerMapping;
    @MockBean AuthService mock0;
    @MockBean CustomerAccountService mock1;
    @MockBean BillingService mock2;
    @MockBean PaymentTransactionService mock3;
    @MockBean ReceiptService mock4;
    @MockBean ServiceCatalogService mock5;
    @MockBean FinanceService mock6;
    @MockBean ApprovalService mock7;
    @MockBean AuditService mock8;
    @MockBean GuestService mock9;
    @MockBean MembershipHistoryService mock10;
    @MockBean EquipmentIncidentService mock11;
    @MockBean InventoryMovementService mock12;
    @MockBean MaintenanceService mock13;
    @MockBean RoomTransferService mock14;
    @MockBean ReservationService mock15;
    @MockBean RoomService mock16;
    @MockBean RoomEquipmentService mock17;
    @MockBean ApprovalAuthorization approvalAuthorization;
    private List<Object> businessMocks() { return List.of(mock0, mock1, mock2, mock3, mock4, mock5, mock6, mock7, mock8, mock9, mock10, mock11, mock12, mock13, mock14, mock15, mock16, mock17); }

    @BeforeEach void responses() {
        when(approvalAuthorization.canApprove(anyLong(), anyString())).thenReturn(true);
        when(mock15.get(1L)).thenReturn(new ReservationDtos.Response(1L, 1L, "actor",
            ReservationStatus.CONFIRMED, ReservationDtos.RentalType.PACKAGE,
            java.math.BigDecimal.ZERO, null, null, null, List.of()));
        businessMocks().forEach(x -> clearInvocations(x));
    }

    record Endpoint(String method, String path, String roles, String body) {
        public String toString() { return method + " " + path; }
    }
    static List<Endpoint> endpoints() { return List.of(
            new Endpoint("POST", "/api/auth/employees", "ADMIN,DIRECTOR,MANAGER", "{\"employee_id\":\"new\",\"full_name\":\"New\",\"password\":\"valid-password\",\"role\":\"STAFF\",\"phone\":\"0900000000\"}"),
            new Endpoint("POST", "/api/auth/employees/emp/password", "ADMIN,DIRECTOR,MANAGER", "{\"password\":\"valid-password\"}"),
            new Endpoint("GET", "/api/guests", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK", "{}"),
            new Endpoint("GET", "/api/guests/1", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK", "{}"),
            new Endpoint("GET", "/api/guests/1/membership-history", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK", "{}"),
            new Endpoint("POST", "/api/guests", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK", "{\"full_name\":\"Guest\",\"identity_number\":\"123456789012\",\"phone\":\"0900000000\"}"),
            new Endpoint("GET", "/api/rooms", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK,HOUSEKEEPING,TECHNICAL,STAFF", "{}"),
            new Endpoint("GET", "/api/rooms/availability?from=2026-10-01T12:00:00&to=2026-10-02T12:00:00", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK,HOUSEKEEPING,TECHNICAL,STAFF", "{}"),
            new Endpoint("PATCH", "/api/rooms/101/status?status=SAN_SANG", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK,HOUSEKEEPING,TECHNICAL", "{}"),
            new Endpoint("GET", "/api/rooms/101/equipment", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK,HOUSEKEEPING,TECHNICAL", "{}"),
            new Endpoint("POST", "/api/rooms/101/equipment", "ADMIN,DIRECTOR,MANAGER,TECHNICAL", "{\"room_id\":\"101\",\"name\":\"TV\",\"original_value\":100,\"purchased_on\":\"2026-01-01\",\"quantity\":1}"),
            new Endpoint("GET", "/api/reservations", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK,HOUSEKEEPING,TECHNICAL,STAFF", "{}"),
            new Endpoint("GET", "/api/reservations/1", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK,HOUSEKEEPING,TECHNICAL,STAFF", "{}"),
            new Endpoint("POST", "/api/reservations", "MANAGER,FRONT_DESK", "{\"guest_id\":1,\"employee_id\":\"actor\",\"deposit\":0,\"rental_type\":\"PACKAGE\",\"rooms\":[{\"room_id\":\"101\",\"expected_check_in\":\"2026-10-01T12:00:00\",\"expected_check_out\":\"2026-10-02T12:00:00\"}]}"),
            new Endpoint("POST", "/api/reservations/1/check-in", "MANAGER,FRONT_DESK", "{}"),
            new Endpoint("POST", "/api/reservations/1/check-out", "MANAGER,FRONT_DESK", "{}"),
            new Endpoint("POST", "/api/reservations/1/cancel", "MANAGER,FRONT_DESK", "{\"reason\":\"Cancelled\"}"),
            new Endpoint("POST", "/api/reservations/1/no-show", "MANAGER,FRONT_DESK", "{}"),
            new Endpoint("POST", "/api/reservations/1/extend", "MANAGER,FRONT_DESK", "{\"new_expected_check_out\":\"2026-10-03T12:00:00\"}"),
            new Endpoint("POST", "/api/reservations/1/services", "MANAGER,FRONT_DESK", "{\"service_id\":\"S1\",\"quantity\":1}"),
            new Endpoint("POST", "/api/reservations/1/equipment-incidents", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK,HOUSEKEEPING", "{\"room_id\":\"101\",\"equipment_name\":\"TV\",\"original_value\":100,\"purchased_at\":\"2026-01-01\",\"quantity\":1}"),
            new Endpoint("POST", "/api/operations/reservations/1/equipment-incidents", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK,HOUSEKEEPING", "{\"room_id\":\"101\",\"equipment_name\":\"TV\",\"original_value\":100,\"purchased_at\":\"2026-01-01\",\"quantity\":1}"),
            new Endpoint("POST", "/api/operations/reservations/1/room-transfers", "MANAGER,FRONT_DESK", "{\"from_room_id\":\"101\",\"to_room_id\":\"102\"}"),
            new Endpoint("GET", "/api/invoices/reservation/1", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK", "{}"),
            new Endpoint("POST", "/api/invoices/reservation/1/deposit/refund", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK", "{}"),
            new Endpoint("POST", "/api/invoices/1/adjust", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{\"delta\":1,\"reason\":\"Correction\"}"),
            new Endpoint("GET", "/api/invoices/1/payments", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK", "{}"),
            new Endpoint("GET", "/api/invoices/1/receipts", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK", "{}"),
            new Endpoint("POST", "/api/invoices/1/payments", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK", "{\"amount\":100,\"method\":\"CASH\",\"type\":\"PAYMENT\",\"idempotency_key\":\"test\"}"),
            new Endpoint("POST", "/api/invoices/1/receipts", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK", "{\"receipt_number\":\"R1\",\"amount\":100,\"method\":\"CASH\"}"),
            new Endpoint("GET", "/api/services", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK,HOUSEKEEPING,KITCHEN", "{}"),
            new Endpoint("POST", "/api/services", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,KITCHEN", "{\"id\":\"S1\",\"name\":\"Water\",\"price\":100,\"opening_stock\":1,\"safety_threshold\":0}"),
            new Endpoint("POST", "/api/services/S1/stock", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK,HOUSEKEEPING,KITCHEN", "{\"quantity\":1}"),
            new Endpoint("GET", "/api/services/S1/inventory-movements", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK,HOUSEKEEPING,KITCHEN", "{}"),
            new Endpoint("POST", "/api/services/S1/inventory-movements", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK,HOUSEKEEPING,KITCHEN", "{\"service_id\":\"S1\",\"type\":\"RECEIPT\",\"quantity\":1}"),
            new Endpoint("GET", "/api/operations/maintenance/room/101", "ADMIN,DIRECTOR,MANAGER,FRONT_DESK,HOUSEKEEPING,TECHNICAL", "{}"),
            new Endpoint("POST", "/api/operations/maintenance", "ADMIN,DIRECTOR,MANAGER,HOUSEKEEPING,TECHNICAL", "{\"id\":\"M1\",\"room_id\":\"101\",\"type\":\"Repair\",\"scheduled_date\":\"2026-10-01\"}"),
            new Endpoint("PATCH", "/api/operations/maintenance/M1/status", "ADMIN,DIRECTOR,MANAGER,HOUSEKEEPING,TECHNICAL", "{\"status\":\"DANG_BAO_TRI\"}"),
            new Endpoint("GET", "/api/finance/cash-handovers", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{}"),
            new Endpoint("GET", "/api/finance/expenses", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{}"),
            new Endpoint("GET", "/api/finance/partner-debts", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{}"),
            new Endpoint("POST", "/api/finance/cash-handovers", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{\"shift_code\":\"SHIFT\",\"from_actor\":\"actor\",\"to_actor\":\"other\",\"actual_amount\":100}"),
            new Endpoint("POST", "/api/finance/expenses", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{\"category\":\"Food\",\"description\":\"Food\",\"amount\":100}"),
            new Endpoint("POST", "/api/finance/partner-debts", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{\"partner_name\":\"Partner\",\"reference_code\":\"D1\",\"amount\":100}"),
            new Endpoint("GET", "/api/governance/approvals", "ADMIN,DIRECTOR,MANAGER", "{}"),
            new Endpoint("POST", "/api/governance/approvals", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING,FRONT_DESK", "{\"action\":\"PRICE_OVERRIDE\",\"target_id\":\"1\",\"payload\":\"{}\",\"reason\":\"Review\"}"),
            new Endpoint("POST", "/api/governance/approvals/1/approve", "ADMIN,DIRECTOR,MANAGER", "{}"),
            new Endpoint("POST", "/api/governance/approvals/1/reject", "ADMIN,DIRECTOR,MANAGER", "{}"),
            new Endpoint("GET", "/api/governance/audit", "ADMIN,DIRECTOR,MANAGER,ACCOUNTING", "{}"),
            new Endpoint("GET", "/api/auth/customers/me", "CUSTOMER", "{}"),
            new Endpoint("POST", "/api/auth/customers/password", "CUSTOMER", "{\"password\":\"valid-password\"}"),
            new Endpoint("POST", "/api/auth/logout", "ADMIN,DIRECTOR,MANAGER,HR,FRONT_DESK,ACCOUNTING,HOUSEKEEPING,TECHNICAL,KITCHEN,STAFF,CUSTOMER,UNKNOWN", "{}")
    ); }
    static Stream<Arguments> matrix() {
        return endpoints().stream().flatMap(endpoint -> Stream.of(
            "ADMIN", "DIRECTOR", "MANAGER", "HR", "FRONT_DESK", "ACCOUNTING", "HOUSEKEEPING",
            "TECHNICAL", "KITCHEN", "STAFF", "CUSTOMER", "UNKNOWN", "ANONYMOUS")
            .map(role -> Arguments.of(endpoint, role)));
    }

    @ParameterizedTest(name = "{1}: {0}") @MethodSource("matrix")
    void roleCanOnlyInvokeAuthorizedDepartmentFunctions(Endpoint endpoint, String role) throws Exception {
        var request = request(HttpMethod.valueOf(endpoint.method()), endpoint.path())
            .contentType("application/json").content(endpoint.body())
            .header("Idempotency-Key", "matrix-key");
        if (!role.equals("ANONYMOUS")) request.with(jwt().jwt(token -> token.subject(role.equals("CUSTOMER") ? "1" : "actor")
            .claim("principal_id", role.equals("CUSTOMER") ? "1" : "actor").claim("principal_type", role.equals("CUSTOMER") ? "CUSTOMER" : "EMPLOYEE"))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role)));
        var response = mvc.perform(request).andReturn().getResponse();
        boolean allowed = Arrays.asList(endpoint.roles().split(",")).contains(role);
        if (allowed) {
            assertThat(response.getStatus()).as(endpoint + " for " + role + ": " + response.getContentAsString())
                .isBetween(200, 299);
            assertThat(businessMocks().stream().mapToInt(x -> mockingDetails(x).getInvocations().size()).sum())
                .as("Authorized request must reach its business service").isPositive();
        } else {
            assertThat(response.getStatus()).as(endpoint + " for " + role + ": " + response.getContentAsString())
                .isEqualTo(role.equals("ANONYMOUS") ? 401 : 403);
            businessMocks().forEach(x -> verifyNoInteractions(x));
        }
    }

    @Test void everyProtectedApiMappingIsCoveredByTheMatrix() {
        Set<String> publicPaths = Set.of("/api/auth/login", "/api/auth/customers/login",
            "/api/auth/refresh", "/api/auth/customers/register");
        Set<String> covered = new HashSet<>();
        for (var entry : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            for (String pattern : entry.getKey().getPatternValues()) {
                if (!pattern.startsWith("/api/") || publicPaths.contains(pattern)) continue;
                for (var method : entry.getKey().getMethodsCondition().getMethods()) {
                    String regex = pattern.replaceAll("\\{[^}]+}", "[^/]+");
                    var matches = endpoints().stream().filter(e -> e.method().equals(method.name())
                        && e.path().split("\\?")[0].matches(regex)).toList();
                    assertThat(matches).as("Uncovered endpoint: " + method + " " + pattern).hasSize(1);
                    covered.add(matches.get(0).toString());
                }
            }
        }
        assertThat(covered).hasSize(endpoints().size());
    }
}
