package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Repository.OllamaResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OllamaResponseService {

    @Autowired
    private OllamaResponseRepository ollamaResponseRepository;
}
