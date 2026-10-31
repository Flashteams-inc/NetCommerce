package com.netcommerce.kernel.core.results;

/** A problem response body (ProblemResult in .NET). */
public record ProblemResult(String type, String title, int status, String detail, String instance) {
}
