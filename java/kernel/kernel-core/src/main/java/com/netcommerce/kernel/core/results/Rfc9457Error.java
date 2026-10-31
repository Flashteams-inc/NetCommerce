package com.netcommerce.kernel.core.results;

import java.util.Map;

/** An RFC 9457 problem detail, as the .NET Rfc9457Error record. */
public record Rfc9457Error(String type, String title, int status, String detail, String instance,
                           Map<String, Object> extensions) {

    public static final Rfc9457Error NONE = new Rfc9457Error("about:blank", "Success", 200, "Operation successful", null, null);

    public static Rfc9457Error notFound(String detail, String instance) {
        return new Rfc9457Error("https://netcommerce.io/probs/not-found", "Resource Not Found", 404, detail, instance, null);
    }

    public static Rfc9457Error validation(String detail, Map<String, Object> validationErrors) {
        return new Rfc9457Error("https://netcommerce.io/probs/validation-error", "Your request is not valid.", 422,
                detail, null, validationErrors);
    }

    public static Rfc9457Error conflict(String detail) {
        return new Rfc9457Error("https://netcommerce.io/probs/conflict", "A conflict occurred.", 409, detail, null, null);
    }
}
