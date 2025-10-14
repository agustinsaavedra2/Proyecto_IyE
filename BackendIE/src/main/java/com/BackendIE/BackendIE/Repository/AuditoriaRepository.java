package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Auditoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends MongoRepository<Auditoria, String>{

}
