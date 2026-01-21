package com.backendie.repository;

import com.backendie.models.OllamaResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OllamaResponseRepository extends MongoRepository<OllamaResponse, String> {
}
