package com.netcommerce.kernel.persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import com.netcommerce.kernel.compliance.pii.PiiSensitive;
import com.netcommerce.kernel.core.domain.MultiTenant;
import com.netcommerce.kernel.core.domain.SoftDelete;

import jakarta.persistence.Convert;

/**
 * Refuses to start when an entity would escape what EF Core applied to every
 * entity of its kind: a tenant-scoped entity without the tenant filter, a
 * soft-deletable one without the not-deleted filter, or a @PiiSensitive field
 * not encrypted by the converter its determinism calls for.
 */
public class KernelMappingCheck implements Integrator {

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        List<String> problems = new ArrayList<>();
        for (PersistentClass entity : metadata.getEntityBindings()) {
            Class<?> type = entity.getMappedClass();
            if (type == null) {
                continue;
            }
            if (MultiTenant.class.isAssignableFrom(type) && !hasFilter(entity, KernelFilters.TENANT)) {
                problems.add(type.getSimpleName() + " is tenant-scoped but has no @Filter(name = KernelFilters.TENANT)");
            }
            if (SoftDelete.class.isAssignableFrom(type) && !hasFilter(entity, KernelFilters.NOT_DELETED)) {
                problems.add(type.getSimpleName() + " is soft-deletable but has no @Filter(name = KernelFilters.NOT_DELETED)");
            }
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    PiiSensitive pii = field.getAnnotation(PiiSensitive.class);
                    if (pii == null) {
                        continue;
                    }
                    Convert convert = field.getAnnotation(Convert.class);
                    Class<?> expected = pii.deterministic() ? DeterministicPiiEncryptionConverter.class : PiiEncryptionConverter.class;
                    if (convert == null || convert.converter() != expected) {
                        problems.add(type.getSimpleName() + "." + field.getName() + " is @PiiSensitive but not @Convert(converter = "
                                + expected.getSimpleName() + ".class)");
                    }
                }
            }
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Kernel persistence rules broken: " + String.join("; ", problems));
        }
    }

    private static boolean hasFilter(PersistentClass entity, String name) {
        for (PersistentClass c = entity; c != null; c = c.getSuperclass()) {
            if (c.getFilters().stream().anyMatch(f -> f.getName().equals(name))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }
}
