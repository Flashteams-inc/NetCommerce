package com.netcommerce.kernel.core.results;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The outcome of an operation without a value. Serialised with the same field
 * names the .NET Result has on the wire (isSuccess, isFailure, error).
 */
@JsonPropertyOrder({"isSuccess", "isFailure", "error"})
public class Result {

    private final boolean success;
    private final Error error;

    protected Result(boolean success, Error error) {
        this.success = success;
        this.error = error;
    }

    public static Result success() {
        return new Result(true, Error.NONE);
    }

    public static Result failure(Error error) {
        return new Result(false, error);
    }

    public static <T> ValueResult<T> success(T value) {
        return new ValueResult<>(value, true, Error.NONE);
    }

    public static <T> ValueResult<T> failure(Error error, Class<T> valueType) {
        return new ValueResult<>(null, false, error);
    }

    /** Success with the value, or the null-value failure when there is none. */
    public static <T> ValueResult<T> create(T value) {
        return value != null ? success(value) : new ValueResult<>(null, false, Error.NULL_VALUE);
    }

    @JsonProperty("isSuccess")
    public boolean isSuccess() {
        return success;
    }

    @JsonProperty("isFailure")
    public boolean isFailure() {
        return !success;
    }

    @JsonProperty("error")
    public Error getError() {
        return error;
    }
}
