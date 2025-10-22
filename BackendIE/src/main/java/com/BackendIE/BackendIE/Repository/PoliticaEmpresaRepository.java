package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.PoliticaEmpresa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliticaEmpresaRepository extends MongoRepository<PoliticaEmpresa, String> {

}
