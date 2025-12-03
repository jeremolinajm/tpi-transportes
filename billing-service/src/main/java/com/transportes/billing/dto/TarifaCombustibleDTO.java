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
public class TarifaCombustibleDTO {

    private Long id;

    @NotNull(message = "El precio por litro es obligatorio")
    @Positive(message = "El precio por litro debe ser positivo")
    private BigDecimal precioPorLitro;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate fechaVigenciaDesde;

    private LocalDate fechaVigenciaHasta;

    private Boolean activa;

    private LocalDateTime fechaCreacion;
}
