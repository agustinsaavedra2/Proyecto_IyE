package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Repository.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuscripcionService {

    @Autowired
    private SuscripcionRepository suscripcionRepository;
}
