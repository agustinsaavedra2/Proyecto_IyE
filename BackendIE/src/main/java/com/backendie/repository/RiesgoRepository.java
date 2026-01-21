package com.backendie.repository;

import com.backendie.models.Riesgo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiesgoRepository extends MongoRepository<Riesgo, String> {
}
