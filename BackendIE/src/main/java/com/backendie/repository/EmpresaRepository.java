package com.backendie.repository;

import com.backendie.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Empresa findById(long id);

    // Añadido: encontrar todas las empresas por categoria
    List<Empresa> findAllByCategoriaId(Long categoriaId);

}
