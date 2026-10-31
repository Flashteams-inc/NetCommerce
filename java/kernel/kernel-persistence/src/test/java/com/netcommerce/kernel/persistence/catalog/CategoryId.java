package com.netcommerce.kernel.persistence.catalog;

import java.util.UUID;

import com.netcommerce.kernel.core.ids.StronglyTypedId;

public record CategoryId(UUID value) implements StronglyTypedId<CategoryId> {
}
