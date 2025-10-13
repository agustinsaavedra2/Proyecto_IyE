package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.OllamaResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OllamaResponseRepository extends JpaRepository<OllamaResponse, Long> {
}
