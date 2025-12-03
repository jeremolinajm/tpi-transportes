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
public class TarifaPesoVolumenDTO {

    private Long id;

    @NotNull(message = "El peso mínimo es obligatorio")
    @Positive(message = "El peso mínimo debe ser positivo")
    private BigDecimal pesoMinimoKg;

    @NotNull(message = "El peso máximo es obligatorio")
    @Positive(message = "El peso máximo debe ser positivo")
    private BigDecimal pesoMaximoKg;

    @NotNull(message = "El volumen mínimo es obligatorio")
    @Positive(message = "El volumen mínimo debe ser positivo")
    private BigDecimal volumenMinimoM3;

    @NotNull(message = "El volumen máximo es obligatorio")
    @Positive(message = "El volumen máximo debe ser positivo")
    private BigDecimal volumenMaximoM3;

    @Positive(message = "El multiplicador de costo debe ser positivo")
    private BigDecimal multiplicadorCosto;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate fechaVigenciaDesde;

    private LocalDate fechaVigenciaHasta;

    private Boolean activa;

    private LocalDateTime fechaCreacion;
}
