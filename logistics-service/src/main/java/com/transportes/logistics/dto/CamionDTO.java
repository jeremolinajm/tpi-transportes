package com.transportes.logistics.dto;

import com.transportes.logistics.entity.Camion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamionDTO {
    private Long id;

    @NotBlank(message = "El dominio es obligatorio")
    @Pattern(regexp = "^[A-Z]{2,3}\\d{3}[A-Z]{2,3}$", message = "Dominio/patente inválido")
    private String dominio;

    private String marca;
    private String modelo;
    private Integer anio;

    @NotNull(message = "La capacidad de peso es obligatoria")
    @Positive(message = "La capacidad de peso debe ser positiva")
    private BigDecimal capacidadPesoKg;

    @NotNull(message = "La capacidad de volumen es obligatoria")
    @Positive(message = "La capacidad de volumen debe ser positiva")
    private BigDecimal capacidadVolumenM3;

    @NotNull(message = "El consumo de combustible es obligatorio")
    @Positive(message = "El consumo de combustible debe ser positivo")
    private BigDecimal consumoCombustibleKmLitro;

    private BigDecimal costoBasePorKm;
    private Long transportistaId;
    private Camion.EstadoCamion estado;
    private Boolean activo;
}
