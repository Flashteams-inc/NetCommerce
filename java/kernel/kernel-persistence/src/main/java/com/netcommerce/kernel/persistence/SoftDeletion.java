package com.netcommerce.kernel.persistence;

import java.time.Instant;

import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.DeleteContext;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import com.netcommerce.kernel.core.domain.SoftDelete;

/**
 * Deleting a soft-deletable entity marks it deleted instead, as AuditInterceptor
 * turns EF Core's delete into an update of DeletedAt; any other entity is
 * deleted as usual. Hibernate loads this integrator itself (META-INF/services).
 */
public class SoftDeletion implements Integrator {

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        var group = registry.getEventListenerGroup(EventType.DELETE);
        var defaults = group.listeners();
        DeleteEventListener delegate = defaults.iterator().next();
        group.clear();
        group.appendListener(new MarkDeleted(delegate));
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

    private static final class MarkDeleted implements DeleteEventListener {

        private final DeleteEventListener delegate;

        private MarkDeleted(DeleteEventListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onDelete(DeleteEvent event) throws HibernateException {
            if (!markDeleted(event)) {
                delegate.onDelete(event);
            }
        }

        @Override
        public void onDelete(DeleteEvent event, DeleteContext transientEntities) throws HibernateException {
            if (!markDeleted(event)) {
                delegate.onDelete(event, transientEntities);
            }
        }

        private static boolean markDeleted(DeleteEvent event) {
            if (event.getObject() instanceof SoftDelete softDelete) {
                softDelete.setDeletedAt(Instant.now());
                return true;
            }
            return false;
        }
    }
}
