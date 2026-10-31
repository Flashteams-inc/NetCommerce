package com.netcommerce.kernel.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Value objects are equal by their components; expected values were captured from the .NET kernel. */
class ValueObjectTest {

    private static final class Money extends ValueObject {
        private final BigDecimal amount;
        private final String currency;

        Money(String amount, String currency) {
            this.amount = new BigDecimal(amount);
            this.currency = currency;
        }

        @Override
        protected List<Object> equalityComponents() {
            return List.of(amount, currency);
        }
    }

    @Test
    void equalComponentsAreEqualAndShareAHashCode() {
        Money a = new Money("10.50", "USD");
        Money b = new Money("10.50", "USD");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void oneDifferentComponentMakesThemDifferent() {
        assertThat(new Money("10.50", "USD")).isNotEqualTo(new Money("10.50", "EUR"));
        assertThat(new Money("10.50", "USD")).isNotEqualTo(new Money("10.51", "USD"));
    }

    @Test
    void valuesOfDifferentTypesAreNeverEqual() {
        assertThat(Email.create("a@b.co")).isNotEqualTo(NonEmptyString.create("a@b.co"));
    }

    @Test
    void theCommonValueObjectsNormaliseAsInDotNet() {
        assertThat(Email.create("  John.Doe@Example.COM ").getValue()).isEqualTo("john.doe@example.com");
        assertThat(PhoneNumber.create("+1 (555) 010-9999").getValue()).isEqualTo("15550109999");
        assertThat(Percentage.fromWholeNumber(18)).hasToString("18.00 %");
        assertThat(Percentage.fromDecimal(new BigDecimal("0.123456")).getValue()).isEqualByComparingTo("0.1235");
        // Half to even, as .NET's Math.Round on a decimal.
        assertThat(Percentage.fromDecimal(new BigDecimal("0.00005")).getValue()).isEqualByComparingTo("0.0000");
        assertThat(Percentage.fromDecimal(new BigDecimal("0.00015")).getValue()).isEqualByComparingTo("0.0002");
        assertThat(Percentage.fromDecimal(new BigDecimal("0.125")).toWholeNumber()).isEqualTo(12);
        assertThat(Percentage.fromDecimal(new BigDecimal("0.135")).toWholeNumber()).isEqualTo(14);
        assertThat(Percentage.fromDecimal(new BigDecimal("0.18"))).isEqualTo(Percentage.fromWholeNumber(18));
    }
}
