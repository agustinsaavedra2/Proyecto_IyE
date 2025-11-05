package com.backendie.repository;

import com.backendie.models.Protocolo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocoloRepository extends MongoRepository<Protocolo, String> {

    List<Protocolo> findByPoliticaId(String politicaId);
}