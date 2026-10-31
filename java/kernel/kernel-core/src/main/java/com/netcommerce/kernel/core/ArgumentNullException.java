package com.netcommerce.kernel.core;

/** A null argument, as .NET's ArgumentNullException: "Value cannot be null. (Parameter '{name}')". */
public class ArgumentNullException extends ArgumentException {

    public ArgumentNullException(String parameterName) {
        super("Value cannot be null.", parameterName);
    }
}
