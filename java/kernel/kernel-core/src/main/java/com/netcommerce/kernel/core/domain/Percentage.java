package com.netcommerce.kernel.core.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * A percentage kept as a fraction (0.18 for 18%), to four places. Rounded half
 * to even, as .NET's Math.Round rounds a decimal by default.
 */
public final class Percentage extends ValueObject {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BigDecimal value;

    private Percentage(BigDecimal value) {
        this.value = value;
    }

    public static Percentage fromDecimal(BigDecimal value) {
        if (value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 1 (Parameter 'value')");
        }
        return new Percentage(value.setScale(4, RoundingMode.HALF_EVEN));
    }

    public static Percentage fromWholeNumber(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100 (Parameter 'value')");
        }
        return new Percentage(BigDecimal.valueOf(value).divide(HUNDRED).setScale(4, RoundingMode.HALF_EVEN));
    }

    public BigDecimal getValue() {
        return value;
    }

    public int toWholeNumber() {
        return value.multiply(HUNDRED).setScale(0, RoundingMode.HALF_EVEN).intValueExact();
    }

    @Override
    protected List<Object> equalityComponents() {
        // Compared by amount, not scale: 0.18 and 0.1800 are the same percentage.
        return List.of(value.stripTrailingZeros());
    }

    /** As .NET formats "{Value:P2}" with the invariant culture: "18.00 %". */
    @Override
    public String toString() {
        return value.multiply(HUNDRED).setScale(2, RoundingMode.HALF_EVEN).toPlainString() + " %";
    }
}
