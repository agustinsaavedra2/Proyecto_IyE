package com.backendie.repository;

import com.backendie.models.Auditoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends MongoRepository<Auditoria, String>{

}
