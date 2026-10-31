package com.netcommerce.kernel.persistence;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs work in one READ COMMITTED transaction and runs it again, in a new
 * transaction, when it fails for a transient reason (ResilientTransaction.cs
 * with Npgsql's EnableRetryOnFailure: up to 5 retries, waits growing to at
 * most 30 seconds). Each attempt starts clean; a failed one is rolled back.
 */
public final class ResilientTransaction {

    /** PostgreSQL states worth retrying: serialisation failure, deadlock, connection loss, shutdown, too many connections. */
    private static final Set<String> TRANSIENT_STATES = Set.of("40001", "40P01", "08000", "08001", "08003", "08004",
            "08006", "57P01", "57P02", "57P03", "53300");

    private final TransactionTemplate template;
    private final int maxRetries;
    private final Duration maxDelay;

    public ResilientTransaction(PlatformTransactionManager transactionManager) {
        this(transactionManager, 5, Duration.ofSeconds(30));
    }

    public ResilientTransaction(PlatformTransactionManager transactionManager, int maxRetries, Duration maxDelay) {
        this.template = new TransactionTemplate(transactionManager);
        this.template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.maxRetries = maxRetries;
        this.maxDelay = maxDelay;
    }

    public void execute(Runnable work) {
        execute(() -> {
            work.run();
            return null;
        });
    }

    public <T> T execute(Supplier<T> work) {
        for (int attempt = 0; ; attempt++) {
            try {
                return template.execute(status -> work.get());
            } catch (RuntimeException e) {
                if (attempt >= maxRetries || !isTransient(e)) {
                    throw e;
                }
                sleep(delay(attempt));
            }
        }
    }

    static boolean isTransient(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof TransientDataAccessException) {
                return true;
            }
            if (t instanceof SQLException sql && sql.getSQLState() != null && TRANSIENT_STATES.contains(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    private Duration delay(int attempt) {
        Duration exponential = Duration.ofMillis((long) (100 * Math.pow(2, attempt)));
        return exponential.compareTo(maxDelay) > 0 ? maxDelay : exponential;
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry", e);
        }
    }
}
