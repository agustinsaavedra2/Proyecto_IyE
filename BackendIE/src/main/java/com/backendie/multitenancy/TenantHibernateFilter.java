package com.backendie.multitenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
public class TenantHibernateFilter {

    @PersistenceContext
    private EntityManager entityManager;

    public void enableCategoriaFilter(Long categoriaId) {
        if (categoriaId == null) return;
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("categoriaFilter");
        filter.setParameter("categoriaId", categoriaId);
    }

    public void disableCategoriaFilter() {
        Session session = entityManager.unwrap(Session.class);
        try {
            session.disableFilter("categoriaFilter");
        } catch (Exception ignored) {}
    }
}

