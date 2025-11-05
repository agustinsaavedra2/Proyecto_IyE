package com.backendie.repository;

import com.backendie.models.CategoriaIndustria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaIndustriaRepository extends JpaRepository<CategoriaIndustria, Long> {

    CategoriaIndustria findById(long id);
}
