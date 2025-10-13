package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Repository.PoliticaEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PoliticaEmpresaService {

    @Autowired
    private PoliticaEmpresaRepository politicaEmpresaRepository;
}
