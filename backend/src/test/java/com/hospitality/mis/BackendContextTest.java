package com.hospitality.mis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:hotelmis;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BackendContextTest {
    @Autowired ApplicationContext context;

    @Test
    void applicationContextExposesTheSharedErrorBoundary() {
        assertThat(context.getBean(com.hospitality.mis.common.exception.GlobalExceptionHandler.class)).isNotNull();
    }
}
