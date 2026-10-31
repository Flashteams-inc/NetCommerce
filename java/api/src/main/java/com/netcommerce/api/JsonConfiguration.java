package com.netcommerce.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netcommerce.kernel.core.ids.StronglyTypedIdModule;

/**
 * The API's JSON: strongly typed ids go out and come in as their bare GUID, as
 * the .NET API's StronglyTypedIdJsonConverterFactory has them. Spring Boot adds
 * every Jackson Module bean to its ObjectMapper.
 */
@Configuration(proxyBeanMethods = false)
class JsonConfiguration {

    @Bean
    StronglyTypedIdModule stronglyTypedIdModule() {
        return new StronglyTypedIdModule();
    }
}
