/**
 * The Payments bounded context. Other contexts may use only the types in this
 * package (its published API) and the events it publishes; its domain,
 * application and infrastructure packages are internal to it.
 */
@ApplicationModule(displayName = "Payments")
package com.netcommerce.payments;

import org.springframework.modulith.ApplicationModule;
