package com.netcommerce.kernel.persistence;

import java.util.UUID;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;

import com.netcommerce.kernel.core.ids.StronglyTypedId;
import com.netcommerce.kernel.core.ids.StronglyTypedIds;

/** One strongly typed id type as Hibernate sees it: a uuid column holding the id's GUID. */
public class StronglyTypedIdJavaType<T extends StronglyTypedId<T>> extends AbstractClassJavaType<T> {

    public StronglyTypedIdJavaType(Class<T> type) {
        super(type, ImmutableMutabilityPlan.instance());
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return indicators.getJdbcType(SqlTypes.UUID);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> X unwrap(T id, Class<X> type, WrapperOptions options) {
        if (id == null) {
            return null;
        }
        if (type.isInstance(id)) {
            return (X) id;
        }
        if (type.isAssignableFrom(UUID.class)) {
            return (X) id.value();
        }
        if (type.isAssignableFrom(String.class)) {
            return (X) id.value().toString();
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> T wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (getJavaTypeClass().isInstance(value)) {
            return getJavaTypeClass().cast(value);
        }
        if (value instanceof UUID uuid) {
            return StronglyTypedIds.create(getJavaTypeClass(), uuid);
        }
        if (value instanceof String text) {
            return StronglyTypedIds.create(getJavaTypeClass(), UUID.fromString(text));
        }
        throw unknownWrap(value.getClass());
    }

    @Override
    public String toString(T id) {
        return id.value().toString();
    }

    @Override
    public T fromString(CharSequence text) {
        return StronglyTypedIds.create(getJavaTypeClass(), UUID.fromString(text.toString()));
    }
}
