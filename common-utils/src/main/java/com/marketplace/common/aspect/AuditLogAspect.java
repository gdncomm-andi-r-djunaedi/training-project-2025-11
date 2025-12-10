package com.marketplace.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Aspect for audit logging of sensitive operations.
 * Logs method execution with:
 * - Action type and description
 * - Correlation ID (from MDC)
 * - Execution time
 * - Success/failure status
 * <p>
 * Usage: Annotate methods with @Auditable(action="ACTION_NAME")
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    private static final String AUDIT_LOGGER_PREFIX = "[AUDIT]";
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Around("@annotation(auditable)")
    public Object auditLog(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        String description = auditable.description();
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        Instant startTime = Instant.now();

        // Log audit start
        log.info("{} action={} method={}.{} correlationId={} status=STARTED description=\"{}\"",
                AUDIT_LOGGER_PREFIX, action, className, methodName,
                correlationId != null ? correlationId : "N/A",
                description.isEmpty() ? action : description);

        try {
            Object result = joinPoint.proceed();

            long durationMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();

            // Log successful completion
            log.info("{} action={} method={}.{} correlationId={} status=SUCCESS durationMs={}",
                    AUDIT_LOGGER_PREFIX, action, className, methodName,
                    correlationId != null ? correlationId : "N/A",
                    durationMs);

            return result;

        } catch (Throwable ex) {
            long durationMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();

            // Log failure
            log.warn("{} action={} method={}.{} correlationId={} status=FAILED durationMs={} error=\"{}\"",
                    AUDIT_LOGGER_PREFIX, action, className, methodName,
                    correlationId != null ? correlationId : "N/A",
                    durationMs,
                    ex.getMessage());

            throw ex;
        }
    }
}
