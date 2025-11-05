package com.backendie.repository;

import com.backendie.models.Regulacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegulacionRepository extends MongoRepository<Regulacion, String> {


}
