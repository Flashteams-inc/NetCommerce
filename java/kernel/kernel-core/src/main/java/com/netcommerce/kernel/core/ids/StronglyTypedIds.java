package com.netcommerce.kernel.core.ids;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Making ids of a given type from a GUID: new, empty, or from a value. */
public final class StronglyTypedIds {

    private static final UUID EMPTY = new UUID(0L, 0L);
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTORS = new ConcurrentHashMap<>();

    private StronglyTypedIds() {
    }

    public static <T extends StronglyTypedId<T>> T create(Class<T> type, UUID value) {
        try {
            return type.cast(constructor(type).newInstance(value));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create " + type.getSimpleName() + " from a GUID", e);
        }
    }

    public static <T extends StronglyTypedId<T>> T newId(Class<T> type) {
        return create(type, UUID.randomUUID());
    }

    public static <T extends StronglyTypedId<T>> T empty(Class<T> type) {
        return create(type, EMPTY);
    }

    private static Constructor<?> constructor(Class<?> type) {
        return CONSTRUCTORS.computeIfAbsent(type, t -> {
            try {
                Constructor<?> c = t.getDeclaredConstructor(UUID.class);
                c.setAccessible(true);
                return c;
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(t.getSimpleName() + " needs a constructor taking its GUID", e);
            }
        });
    }
}
