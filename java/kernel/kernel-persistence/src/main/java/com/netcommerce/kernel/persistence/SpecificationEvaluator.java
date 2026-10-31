package com.netcommerce.kernel.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.query.QueryUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Runs a QuerySpecification as the .NET SpecificationEvaluator runs an
 * ISpecification: the criteria first, then the ordering, then skip and take.
 * Counting and existence use the criteria only.
 */
public final class SpecificationEvaluator {

    private SpecificationEvaluator() {
    }

    public static <T> List<T> list(EntityManager em, Class<T> type, QuerySpecification<T> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        Root<T> root = query.from(type);
        if (spec.criteria() != null) {
            Predicate predicate = spec.criteria().toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        if (spec.order() != null && spec.order().isSorted()) {
            query.orderBy(QueryUtils.toOrders(spec.order(), root, cb));
        }
        TypedQuery<T> typed = em.createQuery(query.select(root));
        if (spec.skip() != null) {
            typed.setFirstResult(spec.skip());
        }
        if (spec.take() != null) {
            typed.setMaxResults(spec.take());
        }
        return typed.getResultList();
    }

    public static <T> long count(EntityManager em, Class<T> type, QuerySpecification<T> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(type);
        if (spec.criteria() != null) {
            Predicate predicate = spec.criteria().toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        return em.createQuery(query.select(cb.count(root))).getSingleResult();
    }

    public static <T> boolean any(EntityManager em, Class<T> type, QuerySpecification<T> spec) {
        return count(em, type, spec) > 0;
    }
}
