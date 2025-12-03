package com.transportes.clients.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorDTO {
    private Long id;
    private String codigo;
    private BigDecimal pesoKg;
    private BigDecimal volumenM3;
    private BigDecimal altoM;
    private BigDecimal anchoM;
    private BigDecimal largoM;
    private String descripcion;
    private Long clienteId;
}
