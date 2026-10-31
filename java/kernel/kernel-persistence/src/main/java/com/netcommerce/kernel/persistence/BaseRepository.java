package com.netcommerce.kernel.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * An aggregate's repository (BaseRepository and SpecificationRepository in .NET):
 * by id, all, add, update and remove from JpaRepository, and queries by
 * specification from JpaSpecificationExecutor, combined with
 * Specification.where(a).and(b). Paged and ordered specifications go through
 * {@link SpecificationEvaluator}.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
