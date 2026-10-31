package com.netcommerce.kernel.core.domain;

/** An aggregate root, with its optimistic-concurrency version (RowVersion in .NET). */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    protected long version;

    protected AggregateRoot() {
    }

    protected AggregateRoot(ID id) {
        super(id);
    }

    public long getVersion() {
        return version;
    }

    protected void raiseDomainEvent(DomainEvent domainEvent) {
        addDomainEvent(domainEvent);
    }
}
