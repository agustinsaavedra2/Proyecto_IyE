package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Regulacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegulacionRepository extends MongoRepository<Regulacion, String> {


}
