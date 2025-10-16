package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.CategoriaIndustria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaIndustriaRepository extends JpaRepository<CategoriaIndustria, Long> {

    CategoriaIndustria findById(long id);
}
