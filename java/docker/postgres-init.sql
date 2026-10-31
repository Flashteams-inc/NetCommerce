-- The databases the AppHost creates (one per bounded context, and Keycloak's),
-- and netcommerce for what the Java monolith keeps across contexts.
CREATE DATABASE catalog;
CREATE DATABASE ordering;
CREATE DATABASE inventory;
CREATE DATABASE payments;
CREATE DATABASE finance;
CREATE DATABASE shipping;
CREATE DATABASE keycloak;
CREATE DATABASE netcommerce;
