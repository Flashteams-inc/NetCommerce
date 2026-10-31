package com.netcommerce.kernel.core.domain;

import java.time.Instant;
import java.util.UUID;

/** A domain event (IDomainEvent): dispatched after the aggregate's change is saved. */
public interface DomainEvent {

    /** This event instance's identifier. */
    UUID eventId();

    /** When it happened, UTC. */
    Instant occurredOn();
}
