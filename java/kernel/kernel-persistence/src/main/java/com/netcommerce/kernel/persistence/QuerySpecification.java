package com.netcommerce.kernel.persistence;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * What to read (ISpecification in .NET): the criteria, one ordering, and the
 * rows to skip and take. Nulls mean none. Related data is fetched by the
 * entity's own mapping rather than by includes.
 */
public record QuerySpecification<T>(Specification<T> criteria, Sort order, Integer skip, Integer take) {

    public static <T> QuerySpecification<T> where(Specification<T> criteria) {
        return new QuerySpecification<>(criteria, Sort.unsorted(), null, null);
    }

    public QuerySpecification<T> orderBy(Sort order) {
        return new QuerySpecification<>(criteria, order, skip, take);
    }

    public QuerySpecification<T> page(int skip, int take) {
        return new QuerySpecification<>(criteria, order, skip, take);
    }
}
