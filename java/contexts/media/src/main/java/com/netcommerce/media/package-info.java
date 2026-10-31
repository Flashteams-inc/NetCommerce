/**
 * The Media bounded context. Other contexts may use only the types in this
 * package (its published API) and the events it publishes; its domain,
 * application and infrastructure packages are internal to it.
 */
@ApplicationModule(displayName = "Media")
package com.netcommerce.media;

import org.springframework.modulith.ApplicationModule;
