package com.netcommerce.kernel.core.results;

/** A failure's code, description and HTTP status, as the .NET Error record. */
public record Error(String code, String description, int statusCode) {

    public static final Error NONE = new Error("", "", 200);
    public static final Error NULL_VALUE = new Error("Error.NullValue", "The specified result value is null", 422);

    public Error(String code, String description) {
        this(code, description, 500);
    }

    public static Error notFound(String entityName, Object id) {
        return new Error(entityName + ".NotFound", entityName + " with id '" + id + "' was not found", 404);
    }

    public static Error validation(String description) {
        return new Error("Validation.Error", description, 422);
    }

    public static Error conflict(String description) {
        return new Error("Conflict.Error", description, 409);
    }

    public static Error unauthorized(String description) {
        return new Error("Unauthorized.Error", description, 401);
    }

    public static Error unauthorized() {
        return unauthorized("Unauthorized access");
    }

    public static Error forbidden(String description) {
        return new Error("Forbidden.Error", description, 403);
    }

    public static Error forbidden() {
        return forbidden("Access forbidden");
    }

    public static Error failure(String code, String description) {
        return new Error(code, description);
    }
}
