package com.backendie.repository;

import com.backendie.models.PoliticaEmpresa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoliticaEmpresaRepository extends MongoRepository<PoliticaEmpresa, String> {

    List<PoliticaEmpresa> findPoliticaEmpresaByEmpresaId(Long empresaId);

}
