package com.netcommerce.kernel.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * The persistence settings every application on this kernel starts with, at the
 * lowest precedence so an application may override them:
 * <ul>
 *   <li>Hibernate validates every mapping against the existing schema and never changes it;</li>
 *   <li>columns are named and quoted as EF Core created them;</li>
 *   <li>the HikariCP pool matches NpgsqlPoolingExtensions: at most 20 connections, at least 2
 *       idle, an idle one closed after 5 minutes, 15 seconds to connect. (Npgsql prunes idle
 *       connections every minute; HikariCP's housekeeping runs every 30 seconds.)</li>
 * </ul>
 */
public class PersistenceDefaults implements EnvironmentPostProcessor {

    static final String SOURCE = "netcommercePersistenceDefaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("spring.jpa.hibernate.ddl-auto", "validate");
        defaults.put("spring.jpa.open-in-view", "false");
        defaults.put("spring.jpa.hibernate.naming.implicit-strategy", EfCoreNamingStrategy.class.getName());
        defaults.put("spring.jpa.hibernate.naming.physical-strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        defaults.put("spring.jpa.properties.hibernate.globally_quoted_identifiers", "true");
        defaults.put("spring.datasource.hikari.maximum-pool-size", "20");
        defaults.put("spring.datasource.hikari.minimum-idle", "2");
        defaults.put("spring.datasource.hikari.idle-timeout", "300000");
        defaults.put("spring.datasource.hikari.connection-timeout", "15000");
        environment.getPropertySources().addLast(new MapPropertySource(SOURCE, defaults));
    }
}
