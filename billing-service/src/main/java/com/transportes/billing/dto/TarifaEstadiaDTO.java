package com.transportes.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaEstadiaDTO {

    private Long id;

    @NotNull(message = "El costo por día es obligatorio")
    @Positive(message = "El costo por día debe ser positivo")
    private BigDecimal costoPorDia;

    @NotNull(message = "El costo por hora es obligatorio")
    @Positive(message = "El costo por hora debe ser positivo")
    private BigDecimal costoPorHora;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate fechaVigenciaDesde;

    private LocalDate fechaVigenciaHasta;

    private Boolean activa;

    private LocalDateTime fechaCreacion;
}
