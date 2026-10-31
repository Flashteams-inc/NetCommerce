package com.netcommerce.kernel.persistence;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import com.netcommerce.kernel.core.ids.StronglyTypedId;

/**
 * Every strongly typed id under com.netcommerce is a uuid column, ids and
 * references alike, with no code per id type: the counterpart of EF Core's
 * StronglyTypedIdConvention. (A JPA AttributeConverter cannot do this: the
 * specification never applies one to an entity's id.) Hibernate loads this
 * contributor itself (META-INF/services).
 */
public class StronglyTypedIdTypes implements TypeContributor {

    static final String BASE_PACKAGE = "com.netcommerce";

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void contribute(TypeContributions contributions, ServiceRegistry serviceRegistry) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition bean) {
                return bean.getMetadata().isIndependent() && bean.getMetadata().isConcrete();
            }
        };
        scanner.addIncludeFilter(new AssignableTypeFilter(StronglyTypedId.class));
        ClassLoader loader = StronglyTypedIdTypes.class.getClassLoader();
        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> type = ClassUtils.resolveClassName(candidate.getBeanClassName(), loader);
            contributions.contributeJavaType(new StronglyTypedIdJavaType(type));
        }
    }
}
