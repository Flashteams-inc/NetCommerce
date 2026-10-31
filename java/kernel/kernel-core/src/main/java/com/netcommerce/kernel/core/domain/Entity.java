package com.netcommerce.kernel.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A domain entity: identity and the domain events it raised. Two entities are
 * equal when their ids are, as in .NET's Entity<TId>.
 */
public abstract class Entity<ID> implements HasDomainEvents {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected ID id;

    protected Entity() {
    }

    protected Entity(ID id) {
        this.id = id;
    }

    public ID getId() {
        return id;
    }

    @Override
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @Override
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public void addDomainEvent(DomainEvent domainEvent) {
        domainEvents.add(domainEvent);
    }

    public void removeDomainEvent(DomainEvent domainEvent) {
        domainEvents.remove(domainEvent);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Entity<?> entity && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
