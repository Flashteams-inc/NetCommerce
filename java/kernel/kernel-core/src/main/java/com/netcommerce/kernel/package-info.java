/**
 * The shared kernel. An open application module: every bounded context may use
 * any of its types, as every .NET context references NetCommerce.Kernel.*.
 */
@ApplicationModule(displayName = "Kernel", type = ApplicationModule.Type.OPEN)
package com.netcommerce.kernel;

import org.springframework.modulith.ApplicationModule;
