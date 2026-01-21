package com.backendie.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Suscribirse {
    private Long empresaId;
    private String plan;
    private Long adminId;
}
