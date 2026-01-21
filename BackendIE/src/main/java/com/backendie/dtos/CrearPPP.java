package com.backendie.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPPP {
    Long empresaId;
    Long usuarioId;
    String politicaId;
    String pregunta;
    String protocoloId;
}
