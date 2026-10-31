package com.netcommerce.kernel.core.domain;

import java.util.List;
import java.util.Locale;

import com.netcommerce.kernel.core.ArgumentException;

/** An email address, trimmed and lower-cased. */
public final class Email extends ValueObject {

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email create(String email) {
        if (email == null || email.isBlank()) {
            throw new ArgumentException("Email cannot be empty", "email");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new ArgumentException("Invalid email format", "email");
        }
        return new Email(email.trim().toLowerCase(Locale.ROOT));
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
