package com.netcommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The composition root, as src/Api is in .NET: one application that hosts every
 * bounded context. Its package is the root Spring Modulith reads the
 * application modules from (com.netcommerce.catalog, com.netcommerce.ordering, ...).
 */
@SpringBootApplication
public class NetCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetCommerceApplication.class, args);
    }
}
