package com.netcommerce.kernel.compliance.pii;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A string field holding personal data, encrypted when stored
 * (PiiSensitiveAttribute in .NET). The persistence layer applies the encrypting
 * converter to every field that carries it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface PiiSensitive {

    /** Deterministic encryption, so equal values can be searched for; off by default. */
    boolean deterministic() default false;

    /** The column of its blind index, for a searchable field; empty for none. */
    String blindIndexColumnName() default "";
}
