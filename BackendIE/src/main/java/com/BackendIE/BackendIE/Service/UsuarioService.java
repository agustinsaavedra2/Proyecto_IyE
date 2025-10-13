package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Usuario;
import com.BackendIE.BackendIE.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
}
