package com.netcommerce.kernel.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * The guards fail with the messages the .NET Guard gives. Every expected message
 * here was captured by running the .NET kernel (NetCommerce.Kernel.Core) itself.
 */
class GuardTest {

    @Test
    void nullAndEmptyInputAreRejectedWithTheDotNetMessages() {
        assertThatThrownBy(() -> Guard.againstNull(null, "name"))
                .isInstanceOf(ArgumentNullException.class)
                .hasMessage("Value cannot be null. (Parameter 'name')");
        assertThatThrownBy(() -> Guard.againstNullOrEmpty("  ", "name"))
                .isInstanceOf(ArgumentException.class)
                .hasMessage("Value cannot be null, empty, or whitespace. (Parameter 'name')");
        assertThatThrownBy(() -> Guard.againstNullOrEmpty((String) null, "name"))
                .hasMessage("Value cannot be null, empty, or whitespace. (Parameter 'name')");
        assertThatThrownBy(() -> Guard.againstNullOrEmpty(List.of(), "items"))
                .hasMessage("Collection cannot be null or empty. (Parameter 'items')");
    }

    @Test
    void rangeGuardsCarryTheActualValueLikeDotNet() {
        assertThatThrownBy(() -> Guard.againstNegative(-1, "quantity"))
                .isInstanceOf(ArgumentOutOfRangeException.class)
                .hasMessage("Value cannot be negative. (Parameter 'quantity')\nActual value was -1.");
        assertThatThrownBy(() -> Guard.againstNegative(new BigDecimal("-2.5"), "amount"))
                .hasMessage("Value cannot be negative. (Parameter 'amount')\nActual value was -2.5.");
        assertThatThrownBy(() -> Guard.againstNegativeOrZero(0, "count"))
                .hasMessage("Value must be positive. (Parameter 'count')\nActual value was 0.");
        assertThatThrownBy(() -> Guard.againstOutOfRange(11, 1, 10, "rating"))
                .hasMessage("Value must be between 1 and 10. (Parameter 'rating')\nActual value was 11.");
    }

    @Test
    void theOtherGuardsMatchToo() {
        assertThatThrownBy(() -> Guard.againstDefault(new UUID(0L, 0L), "id"))
                .hasMessage("GUID cannot be empty. (Parameter 'id')");
        assertThatThrownBy(() -> Guard.againstOverflow("abcdef", 5, "code"))
                .hasMessage("String cannot exceed 5 characters. (Parameter 'code')");
        assertThatThrownBy(() -> Guard.against(true, "Custom rule broken.", "rule"))
                .hasMessage("Custom rule broken. (Parameter 'rule')");
        assertThatThrownBy(() -> Guard.ensure(false, "State is wrong."))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("State is wrong.");
    }
}
