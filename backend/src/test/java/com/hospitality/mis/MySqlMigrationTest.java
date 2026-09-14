package com.hospitality.mis;



import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;


import static org.assertj.core.api.Assertions.assertThat;



/** Thực thi migration MySQL thực tế và kiểm tra Hibernate; yêu cầu một cơ sở dữ liệu trống dùng tạm thời. */

@SpringBootTest(properties = {
        "spring.datasource.url=${MIGRATION_TEST_DB_URL}",
        "spring.datasource.username=${MIGRATION_TEST_DB_USERNAME}",
        "spring.datasource.password=${MIGRATION_TEST_DB_PASSWORD}",
        "spring.flyway.enabled=true",

        "spring.flyway.baseline-on-migrate=false",

        "spring.jpa.hibernate.ddl-auto=validate"

})

class MySqlMigrationTest {
    /** Flyway thật dùng kiểm tra migration line và pending state. */
    @Autowired Flyway flyway;
    /** DataSource thật dùng kiểm tra product name và metadata SQL. */
    @Autowired DataSource dataSource;


    @Test

    /** Given database MySQL rỗng, When validate/info, Then migration line đầy đủ và JPA validate thành công. */
    void cleanMySqlReceivesTheCompleteMigrationLineAndJpaMappingsValidate() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualToIgnoringCase("MySQL");
        }
        var validation = flyway.validateWithResult();
        assertThat(validation.validationSuccessful).isTrue();
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().applied()).isNotEmpty();
    }

    @Test
    /** Given phone columns production, When đọc metadata, Then NOT NULL và unique được bảo vệ bằng SQL. */
    void employeeAndCustomerPhoneColumnsAreNotNullAndUniqueInSql() {
        assertPhoneConstraint("employees");
        assertPhoneConstraint("customer_accounts");
    }

    /** Kiểm tra metadata column/index theo table được truyền, không sửa schema trong test. */
    private void assertPhoneConstraint(String tableName) {
        try (var connection = getConnection()) {
            try (var columns = connection.prepareStatement(
                    "select is_nullable from information_schema.columns "
                            + "where table_schema = database() and table_name = ? and column_name = 'phone'")) {
                columns.setString(1, tableName);
                try (var result = columns.executeQuery()) {
                    assertThat(result.next()).as("phone column in %s", tableName).isTrue();
                    assertThat(result.getString("is_nullable")).isEqualTo("NO");
                }
            }
            try (var indexes = connection.prepareStatement(
                    "select count(*) from information_schema.statistics "
                            + "where table_schema = database() and table_name = ? "
                            + "and column_name = 'phone' and non_unique = 0")) {
                indexes.setString(1, tableName);
                try (var result = indexes.executeQuery()) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getInt(1)).as("unique index on %s.phone", tableName).isGreaterThan(0);
                }
            }
        } catch (Exception e) {
            throw new AssertionError("Cannot inspect SQL constraints for " + tableName, e);
        }
    }

    /** Mở connection từ DataSource cho các assertion metadata SQL. */
    private java.sql.Connection getConnection() throws java.sql.SQLException {
        return dataSource.getConnection();
    }
}
