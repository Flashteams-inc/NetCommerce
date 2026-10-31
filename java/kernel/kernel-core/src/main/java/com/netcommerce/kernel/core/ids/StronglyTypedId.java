package com.netcommerce.kernel.core.ids;

import java.util.UUID;

/**
 * An id that is its own type but a GUID underneath (IStronglyTypedId in .NET):
 * declare one as a record, e.g. {@code record OrderId(UUID value) implements StronglyTypedId<OrderId>}.
 * On the wire it is the bare GUID string (StronglyTypedIdModule), as in .NET.
 */
public interface StronglyTypedId<T extends StronglyTypedId<T>> extends Comparable<T> {

    UUID value();

    @Override
    default int compareTo(T other) {
        return value().compareTo(other.value());
    }
}
