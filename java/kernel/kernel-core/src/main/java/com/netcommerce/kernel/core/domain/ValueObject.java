package com.netcommerce.kernel.core.domain;

import java.util.List;
import java.util.Objects;

/**
 * A value object: equal to another of the same type whose components are equal,
 * in order, as .NET's ValueObject compares GetEqualityComponents().
 */
public abstract class ValueObject {

    protected abstract List<Object> equalityComponents();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return equalityComponents().equals(((ValueObject) other).equalityComponents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(equalityComponents().toArray());
    }
}
