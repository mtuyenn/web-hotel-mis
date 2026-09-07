package com.hospitality.mis.identity;

import com.hospitality.mis.identity.adapter.EmployeeRepository;
import com.hospitality.mis.identity.domain.Employee;
import com.hospitality.mis.reservation.domain.Reservation;
import com.hospitality.mis.identity.domain.EmployeeRole;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:identitymapping;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IdentityPersistenceTest {
    @Autowired
    EmployeeRepository employees;

    @Test
    void employeeRepositoryPersistsCanonicalEmployeeSchema() {
        Employee employee = new Employee();
        employee.setEmployeeId("ID01");
        employee.setFullName("Identity Employee");
        employee.setPassword("$2a$12$" + "A".repeat(53));
        employee.setRole(EmployeeRole.FRONT_DESK);
        employee.setAddress("Front desk");
        employee.setPhone("0909000001");

        Employee saved = employees.saveAndFlush(employee);

        assertThat(saved.getEmployeeId()).isEqualTo("ID01");
        assertThat(saved.getFullName()).isEqualTo("Identity Employee");
        assertThat(saved.getRole()).isEqualTo(EmployeeRole.FRONT_DESK);
        assertThat(employees.findById("ID01")).get()
                .extracting(Employee::getPhone)
                .isEqualTo("0909000001");
    }

    @Test
    void employeeIsTheConcreteJpaOwner() throws Exception {
        assertThat(Employee.class.isAnnotationPresent(Entity.class)).isTrue();
        assertThat(Employee.class.getAnnotation(Table.class).name()).isEqualTo("employees");

        assertColumn("employeeId", "id", 10);
        assertColumn("fullName", "full_name", 0);
        assertColumn("password", "password", 255);
        assertColumn("role", "position", 0);
        assertColumn("address", "address", 0);
        assertColumn("phone", "phone", 15);

        Field reservationEmployee = Reservation.class.getDeclaredField("employee");
        assertThat(reservationEmployee.getType()).isEqualTo(Employee.class);
        assertThat(reservationEmployee.getAnnotation(ManyToOne.class)).isNotNull();
        assertThat(reservationEmployee.getAnnotation(JoinColumn.class).name()).isEqualTo("employee_id");

        Field employeeReservations = Employee.class.getDeclaredField("reservations");
        OneToMany inverse = employeeReservations.getAnnotation(OneToMany.class);
        assertThat(inverse).isNotNull();
        assertThat(inverse.mappedBy()).isEqualTo("employee");
        assertThat(inverse.cascade()).isEmpty();
        assertThat(inverse.orphanRemoval()).isFalse();
    }

    private void assertColumn(String fieldName, String columnName, int length) throws Exception {
        Column column = Employee.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertThat(column).as(fieldName).isNotNull();
        assertThat(column.name()).isEqualTo(columnName);
        if (length > 0) {
            assertThat(column.length()).isEqualTo(length);
        }
    }
}
