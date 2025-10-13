package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Repository.RiesgoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiesgoService {

    @Autowired
    private RiesgoRepository riesgoRepository;
}
