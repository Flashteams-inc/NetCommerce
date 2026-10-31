package com.netcommerce.kernel.core.domain;

import java.time.Instant;
import java.util.UUID;

/** A domain event with a fresh id and the current time (DomainEvent in .NET). */
public abstract class BaseDomainEvent implements DomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredOn = Instant.now();

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}
