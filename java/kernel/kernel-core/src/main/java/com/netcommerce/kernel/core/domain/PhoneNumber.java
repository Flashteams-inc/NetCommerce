package com.netcommerce.kernel.core.domain;

import java.util.List;

import com.netcommerce.kernel.core.ArgumentException;

/** A phone number, kept as its digits only (7 to 15 of them). */
public final class PhoneNumber extends ValueObject {

    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber create(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new ArgumentException("Phone number cannot be empty", "phoneNumber");
        }
        StringBuilder digits = new StringBuilder();
        phoneNumber.codePoints().filter(Character::isDigit).forEach(digits::appendCodePoint);
        if (digits.length() < 7 || digits.length() > 15) {
            throw new ArgumentException("Invalid phone number length", "phoneNumber");
        }
        return new PhoneNumber(digits.toString());
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
