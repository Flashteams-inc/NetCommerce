/**
 * The Shipping bounded context. Other contexts may use only the types in this
 * package (its published API) and the events it publishes; its domain,
 * application and infrastructure packages are internal to it.
 */
@ApplicationModule(displayName = "Shipping")
package com.netcommerce.shipping;

import org.springframework.modulith.ApplicationModule;
