package com.transportes.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoSolicitudDTO {

    private Long id;

    private Long solicitudId;

    private String tipo;

    private BigDecimal costoGestion;

    private BigDecimal costoTransporte;

    private BigDecimal costoCombustible;

    private BigDecimal costoEstadia;

    private BigDecimal costoAdicionales;

    private BigDecimal costoTotal;

    private Long tarifaBaseId;

    private Long tarifaCombustibleId;

    private Long tarifaEstadiaId;

    private LocalDateTime fechaCalculo;

    private String observaciones;

    private List<CostoTramoDTO> costosTramos;
}
