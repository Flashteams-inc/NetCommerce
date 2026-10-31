package com.netcommerce.kernel.core.application;

import java.util.Collection;

/** Who is making the current request (IUserContext in NetCommerce.Kernel.Application). */
public interface UserContext {

    String getUserId();

    /** The tenant the user acts for; null outside a tenant. */
    String getTenantId();

    boolean isAuthenticated();

    /** A claim's value, or null when the user has no such claim. */
    String getClaim(String claimType);

    Collection<String> getRoles();

    boolean isInRole(String role);
}
