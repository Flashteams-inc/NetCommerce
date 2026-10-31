package com.netcommerce;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

/**
 * The bounded contexts stay apart: each may use another only through its
 * published API and events, as the .NET contexts reach each other only through
 * their contracts.
 */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(NetCommerceApplication.class);

    @Test
    void everyBoundedContextIsAnApplicationModule() {
        Set<String> names = modules.stream().map(ApplicationModule::getIdentifier)
                .map(Object::toString).collect(Collectors.toSet());
        assertThat(names).contains("catalog", "basket", "media", "inventory",
                "shipping", "ordering", "payments", "finance", "kernel");
    }

    @Test
    void noModuleReachesIntoAnothersInternals() {
        modules.verify();
    }
}
