package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Procedimiento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedimientoRepository extends MongoRepository<Procedimiento, String> {

    List<Procedimiento> findByProtocoloId(String protocoloId);
}