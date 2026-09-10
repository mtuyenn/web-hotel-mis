package com.hospitality.mis;



import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;


import static org.assertj.core.api.Assertions.assertThat;



/** Runs real MySQL migrations and Hibernate validation; requires a disposable empty database. */

@SpringBootTest(properties = {
        "spring.datasource.url=${MIGRATION_TEST_DB_URL}",
        "spring.datasource.username=${MIGRATION_TEST_DB_USERNAME}",
        "spring.datasource.password=${MIGRATION_TEST_DB_PASSWORD}",
        "spring.flyway.enabled=true",

        "spring.flyway.baseline-on-migrate=false",

        "spring.jpa.hibernate.ddl-auto=validate"

})

class MySqlMigrationTest {
    @Autowired Flyway flyway;
    @Autowired DataSource dataSource;


    @Test

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
    void employeeAndCustomerPhoneColumnsAreNotNullAndUniqueInSql() {
        assertPhoneConstraint("employees");
        assertPhoneConstraint("customer_accounts");
    }

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

    private java.sql.Connection getConnection() throws java.sql.SQLException {
        return dataSource.getConnection();
    }
}
