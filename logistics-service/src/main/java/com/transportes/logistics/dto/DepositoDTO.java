package com.transportes.logistics.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositoDTO {
    private Long id;

    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
    private BigDecimal latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
    private BigDecimal longitud;

    @Positive(message = "La capacidad máxima debe ser positiva")
    private Integer capacidadMaxima;

    private Integer contenedoresActuales;

    private Boolean activo;
}
