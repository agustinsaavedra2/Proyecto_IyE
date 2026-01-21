package com.backendie.service;

import com.backendie.models.Riesgo;
import com.backendie.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.ollama.OllamaChatModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RiesgoServiceTest {

    RiesgoRepository riesgoRepository;
    EmpresaRepository empresaRepository;
    PoliticaEmpresaRepository politicaEmpresaRepository;
    ProtocoloRepository protocoloRepository;
    ProcedimientoRepository procedimientoRepository;
    UsuarioRepository usuarioRepository;
    OllamaChatModel ollamaChatModel;
    OllamaResponseRepository ollamaResponseRepository;

    RiesgoService riesgoService;

    @BeforeEach
    void setup() {
        riesgoRepository = mock(RiesgoRepository.class);
        empresaRepository = mock(EmpresaRepository.class);
        politicaEmpresaRepository = mock(PoliticaEmpresaRepository.class);
        protocoloRepository = mock(ProtocoloRepository.class);
        procedimientoRepository = mock(ProcedimientoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        ollamaChatModel = mock(OllamaChatModel.class);
        ollamaResponseRepository = mock(OllamaResponseRepository.class);

        riesgoService = new RiesgoService(riesgoRepository, empresaRepository, politicaEmpresaRepository, protocoloRepository, procedimientoRepository, usuarioRepository, ollamaChatModel, ollamaResponseRepository);
    }

    @Test
    void generarRiesgos_shouldThrowWhenMissingParams() {
        assertThrows(IllegalArgumentException.class, () -> riesgoService.generarRiesgos(null, 1L, List.of(1L)));
        assertThrows(IllegalArgumentException.class, () -> riesgoService.generarRiesgos(1L, null, List.of(1L)));
        assertThrows(IllegalArgumentException.class, () -> riesgoService.generarRiesgos(1L, 1L, List.of()));
    }

}

