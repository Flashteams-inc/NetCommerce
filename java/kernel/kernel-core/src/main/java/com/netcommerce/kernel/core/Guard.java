package com.netcommerce.kernel.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

/**
 * Argument guards, as src/Kernel/NetCommerce.Kernel.Core/Guard.cs, with the same
 * messages. Java has no caller-expression capture, so each guard takes the
 * parameter's name.
 */
public final class Guard {

    private Guard() {
    }

    public static <T> T againstNull(T value, String parameterName) {
        if (value == null) {
            throw new ArgumentNullException(parameterName);
        }
        return value;
    }

    public static String againstNullOrEmpty(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            throw new ArgumentException("Value cannot be null, empty, or whitespace.", parameterName);
        }
        return value;
    }

    public static <C extends Collection<?>> C againstNullOrEmpty(C value, String parameterName) {
        if (value == null || value.isEmpty()) {
            throw new ArgumentException("Collection cannot be null or empty.", parameterName);
        }
        return value;
    }

    public static BigDecimal againstNegative(BigDecimal value, String parameterName) {
        if (value.signum() < 0) {
            throw new ArgumentOutOfRangeException(parameterName, value.toPlainString(), "Value cannot be negative.");
        }
        return value;
    }

    public static int againstNegative(int value, String parameterName) {
        if (value < 0) {
            throw new ArgumentOutOfRangeException(parameterName, value, "Value cannot be negative.");
        }
        return value;
    }

    public static BigDecimal againstNegativeOrZero(BigDecimal value, String parameterName) {
        if (value.signum() <= 0) {
            throw new ArgumentOutOfRangeException(parameterName, value.toPlainString(), "Value must be positive.");
        }
        return value;
    }

    public static int againstNegativeOrZero(int value, String parameterName) {
        if (value <= 0) {
            throw new ArgumentOutOfRangeException(parameterName, value, "Value must be positive.");
        }
        return value;
    }

    public static <T extends Comparable<T>> T againstOutOfRange(T value, T minimum, T maximum, String parameterName) {
        if (value.compareTo(minimum) < 0 || value.compareTo(maximum) > 0) {
            throw new ArgumentOutOfRangeException(parameterName, value,
                    "Value must be between " + minimum + " and " + maximum + ".");
        }
        return value;
    }

    public static UUID againstDefault(UUID value, String parameterName) {
        if (value == null || new UUID(0L, 0L).equals(value)) {
            throw new ArgumentException("GUID cannot be empty.", parameterName);
        }
        return value;
    }

    public static String againstOverflow(String value, int maxLength, String parameterName) {
        if (value.length() > maxLength) {
            throw new ArgumentException("String cannot exceed " + maxLength + " characters.", parameterName);
        }
        return value;
    }

    public static void against(boolean condition, String message, String parameterName) {
        if (condition) {
            throw new ArgumentException(message, parameterName);
        }
    }

    /** As .NET's Ensure: a broken invariant is an invalid state, not a bad argument. */
    public static void ensure(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
