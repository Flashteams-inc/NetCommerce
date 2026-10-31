package com.netcommerce.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** The search engine's health on /actuator/health, as "meilisearch": its own /health says "available". */
@Component("meilisearch")
class MeilisearchHealthIndicator implements HealthIndicator {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final URI health;

    MeilisearchHealthIndicator(@Value("${netcommerce.meilisearch.url}") String url) {
        this.health = URI.create(url.replaceAll("/+$", "") + "/health");
    }

    @Override
    public Health health() {
        try {
            HttpResponse<String> response = http.send(HttpRequest.newBuilder(health).timeout(Duration.ofSeconds(2)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            boolean available = response.statusCode() == 200 && response.body().contains("\"available\"");
            return (available ? Health.up() : Health.down()).withDetail("url", health.toString()).build();
        } catch (Exception e) {
            return Health.down(e).withDetail("url", health.toString()).build();
        }
    }
}
