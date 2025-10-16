package com.BackendIE.BackendIE.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearAuditoria {
    Long empresaId;
    String tipo;
    String objetivo;
    Long auditorLiderId;
    List<Long> idsDePoliticasAEvaluar;
}
