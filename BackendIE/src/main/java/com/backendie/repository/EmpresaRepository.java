package com.backendie.repository;

import com.backendie.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Empresa findById(long id);

    // find all empresas by categoria
    List<Empresa> findAllByCategoriaId(Long categoriaId);

    @Query("select e from Empresa e where e.categoriaId = :categoriaId")
    List<Empresa> findResumenByCategoria(@Param("categoriaId") Long categoriaId);

}
