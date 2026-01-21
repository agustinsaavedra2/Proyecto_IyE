package com.backendie.multitenancy;

import org.springframework.stereotype.Component;

@Component
public class TenantHibernateFilter {

    // NO-OP: do not enable/disable Hibernate filters here. Tenant filtering will be explicit in repos/services.
    public void enableCategoriaFilter(Long categoriaId) {
        // no-op
    }

    public void disableCategoriaFilter() {
        // no-op
    }
}
