package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Riesgo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiesgoRepository extends MongoRepository<Riesgo, String> {
}
