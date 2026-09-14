package com.hospitality.mis.governance;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.governance.IdempotencyRecordRepository;
import com.hospitality.mis.dao.governance.IdempotencyLockBucketRepository;
import com.hospitality.mis.service.governance.DurableIdempotencyService;
import com.hospitality.mis.entity.governance.IdempotencyLockBucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:durable-idempotency;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DurableIdempotencyServiceTest {
    @Autowired DurableIdempotencyService service;
    @Autowired IdempotencyRecordRepository records;
    @Autowired TransactionTemplate transactions;
    @Autowired IdempotencyLockBucketRepository lockBuckets;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clear() {
        records.deleteAll();
        for (int value = 0; value < 64; value++) {
            jdbc.update("MERGE INTO idempotency_lock_buckets(bucket_id) KEY(bucket_id) VALUES (?)", value);
        }
    }

    @Test
    void committedResultIsReplayedWithoutRunningCommandAgain() {
        AtomicInteger executions = new AtomicInteger();
        TestResponse first = transactions.execute(status -> service.execute("test-command", "same-key", "actor-1",
                "hash-1", TestResponse.class, () -> new TestResponse(executions.incrementAndGet(), "ok")));
        TestResponse replay = transactions.execute(status -> service.execute("test-command", "same-key", "actor-1",
                "hash-1", TestResponse.class, () -> new TestResponse(executions.incrementAndGet(), "duplicate")));

        assertThat(first).isEqualTo(new TestResponse(1, "ok"));
        assertThat(replay).isEqualTo(first);
        assertThat(executions).hasValue(1);
        assertThat(records.count()).isEqualTo(1);
    }

    @Test
    void sameKeyCannotBeReusedForAnotherActorOrPayload() {
        transactions.executeWithoutResult(status -> service.execute("test-command", "same-key", "actor-1",
                "hash-1", TestResponse.class, () -> new TestResponse(1, "ok")));

        assertThatThrownBy(() -> transactions.execute(status -> service.execute("test-command", "same-key", "actor-2",
                "hash-2", TestResponse.class, () -> new TestResponse(2, "bad"))))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void concurrentCallsWithTheSameNewKeyExecuteOnlyOnce() throws Exception {
        AtomicInteger executions = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var command = (java.util.concurrent.Callable<TestResponse>) () -> {
                ready.countDown();
                assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
                return transactions.execute(status -> service.execute("concurrent-command", "new-key", "actor-1",
                        "hash-1", TestResponse.class, () -> {
                            int value = executions.incrementAndGet();
                            try { Thread.sleep(100); } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();
                                throw new IllegalStateException(exception);
                            }
                            return new TestResponse(value, "ok");
                        }));
            };
            var first = executor.submit(command);
            var second = executor.submit(command);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(first.get(10, TimeUnit.SECONDS)).isEqualTo(new TestResponse(1, "ok"));
            assertThat(second.get(10, TimeUnit.SECONDS)).isEqualTo(new TestResponse(1, "ok"));
        }
        assertThat(executions).hasValue(1);
    }

    public record TestResponse(int value, String message) {}
}
