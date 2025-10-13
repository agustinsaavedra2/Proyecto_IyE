package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Repository.CategoriaIndustriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoriaIndustriaService {

    @Autowired
    private CategoriaIndustriaRepository categoriaIndustriaRepository;
}
