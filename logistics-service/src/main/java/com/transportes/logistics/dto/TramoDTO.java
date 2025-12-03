package com.transportes.logistics.dto;

import com.transportes.logistics.entity.Tramo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoDTO {
    private Long id;

    @NotNull(message = "El número de orden es obligatorio")
    @Positive(message = "El número de orden debe ser positivo")
    private Integer numeroOrden;

    private Tramo.TipoTramo tipoTramo;
    private String origenDireccion;
    private String destinoDireccion;

    @NotNull(message = "La distancia es obligatoria")
    @Positive(message = "La distancia debe ser positiva")
    private BigDecimal distanciaKm;

    private Tramo.EstadoTramo estado;
    private Long camionId;
    private String camionDominio;
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
    private BigDecimal costoEstimado;
    private BigDecimal costoReal;
}
