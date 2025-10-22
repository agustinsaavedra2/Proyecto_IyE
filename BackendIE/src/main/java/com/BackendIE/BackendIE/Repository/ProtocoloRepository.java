package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Protocolo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocoloRepository extends MongoRepository<Protocolo, String> {

    List<Protocolo> findByIdPolitica(String idPolitica);
}