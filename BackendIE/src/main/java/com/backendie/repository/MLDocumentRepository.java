package com.backendie.repository;

import com.backendie.models.MLDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MLDocumentRepository extends MongoRepository<MLDocument, String> {
    List<MLDocument> findByEmpresaId(Long empresaId);
}

