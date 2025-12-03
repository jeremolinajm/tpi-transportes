package com.transportes.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoTramoDTO {

    private Long id;

    private Long tramoId;

    private String tipo;

    private BigDecimal distanciaKm;

    private BigDecimal costoPorKm;

    private BigDecimal costoCombustible;

    private BigDecimal costoEstadia;

    private BigDecimal horasEstadia;

    private BigDecimal costoTotalTramo;

    private LocalDateTime fechaCalculo;
}
