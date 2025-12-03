package com.transportes.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class TarifaBaseDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "El costo fijo de gestión es obligatorio")
    @Positive(message = "El costo fijo de gestión debe ser positivo")
    private BigDecimal costoFijoGestion;

    @Positive(message = "El costo adicional por tramo debe ser positivo")
    private BigDecimal costoAdicionalPorTramo;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate fechaVigenciaDesde;

    private LocalDate fechaVigenciaHasta;

    private Boolean activa;

    private LocalDateTime fechaCreacion;
}
