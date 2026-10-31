package com.netcommerce.kernel.core.results;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** The outcome of an operation with a value (Result<TValue> in .NET). */
@JsonPropertyOrder({"value", "isSuccess", "isFailure", "error"})
public class ValueResult<T> extends Result {

    private final T value;

    ValueResult(T value, boolean success, Error error) {
        super(success, error);
        this.value = value;
    }

    /** The value; reading it from a failure is a programming error, as in .NET. */
    @JsonProperty("value")
    public T getValue() {
        if (!isSuccess()) {
            throw new IllegalStateException("Cannot access Value of a failure result. Error: " + getError().description());
        }
        return value;
    }
}
