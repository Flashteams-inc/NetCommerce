package com.netcommerce.kernel.core;

/**
 * An argument outside its range, as .NET's ArgumentOutOfRangeException:
 * "{message} (Parameter '{name}')" and, on a new line, "Actual value was {value}.".
 */
public class ArgumentOutOfRangeException extends IllegalArgumentException {

    private final String parameterName;
    private final Object actualValue;

    public ArgumentOutOfRangeException(String parameterName, Object actualValue, String message) {
        super(message + " (Parameter '" + parameterName + "')\nActual value was " + actualValue + ".");
        this.parameterName = parameterName;
        this.actualValue = actualValue;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Object getActualValue() {
        return actualValue;
    }
}
