package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Repository.RegulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegulacionService {

    @Autowired
    private RegulacionRepository regulacionRepository;
}
