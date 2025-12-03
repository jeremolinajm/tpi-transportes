package com.transportes.logistics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaDTO {
    private Long id;
    private Long solicitudId;
    private String tipo;
    private Integer indice; // Índice para identificar la ruta cuando no está guardada en BD
    private String descripcion; // Descripción de la ruta (ej: "Ruta directa", "Con 1 depósito")
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private BigDecimal distanciaTotalKm;
    private Integer tiempoEstimadoTotalHoras;
    private BigDecimal costoEstimadoTotal;
    private Boolean seleccionada;
    private List<TramoDTO> tramos;
}
