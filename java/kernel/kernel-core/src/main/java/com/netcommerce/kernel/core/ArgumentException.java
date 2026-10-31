package com.netcommerce.kernel.core;

/**
 * An invalid argument, worded as .NET's ArgumentException words it:
 * "{message} (Parameter '{name}')", so messages read the same on both runtimes.
 */
public class ArgumentException extends IllegalArgumentException {

    private final String parameterName;

    public ArgumentException(String message, String parameterName) {
        super(parameterName == null ? message : message + " (Parameter '" + parameterName + "')");
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}
