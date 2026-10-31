package com.netcommerce.kernel.core.domain;

import java.util.List;

import com.netcommerce.kernel.core.ArgumentException;

/** A string with something in it, trimmed. */
public final class NonEmptyString extends ValueObject {

    private final String value;

    private NonEmptyString(String value) {
        this.value = value;
    }

    public static NonEmptyString create(String value) {
        if (value == null || value.isBlank()) {
            throw new ArgumentException("Value cannot be null or empty", "value");
        }
        return new NonEmptyString(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    protected List<Object> equalityComponents() {
        return List.of(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
