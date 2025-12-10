package com.marketplace.common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for audit logging.
 * Methods annotated with this will have their execution logged
 * with user information, action type, and timestamps.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The action being performed (e.g., "LOGIN", "REGISTER", "ADD_TO_CART")
     */
    String action();

    /**
     * Description of the action for logging purposes
     */
    String description() default "";
}
