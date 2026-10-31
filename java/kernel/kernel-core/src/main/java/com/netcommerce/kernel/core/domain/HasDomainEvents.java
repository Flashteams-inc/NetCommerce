package com.netcommerce.kernel.core.domain;

import java.util.List;

/** Something that collects domain events until they are dispatched. */
public interface HasDomainEvents {

    List<DomainEvent> getDomainEvents();

    void clearDomainEvents();
}
