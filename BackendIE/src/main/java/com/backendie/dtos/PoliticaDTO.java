package com.backendie.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PoliticaDTO {
    private String id;
    private String titulo;
}
