package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.OllamaResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OllamaResponseRepository extends MongoRepository<OllamaResponse, String> {
}
