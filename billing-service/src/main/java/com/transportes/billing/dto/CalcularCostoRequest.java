package com.transportes.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalcularCostoRequest {

    @NotNull(message = "El ID de solicitud es obligatorio")
    private Long solicitudId;

    @NotEmpty(message = "Debe incluir al menos un tramo")
    @Valid
    private List<TramoRequest> tramos;

    @Positive(message = "El peso total debe ser positivo")
    private BigDecimal pesoTotalKg;

    @Positive(message = "El volumen total debe ser positivo")
    private BigDecimal volumenTotalM3;

    @PositiveOrZero(message = "Los días de estadía no pueden ser negativos")
    private Integer diasEstadiaEstimados;

    @PositiveOrZero(message = "Las horas de estadía no pueden ser negativas")
    private BigDecimal horasEstadiaTotales;

    @PositiveOrZero(message = "Los costos adicionales no pueden ser negativos")
    private BigDecimal costosAdicionales;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TramoRequest {

        @NotNull(message = "El ID del tramo es obligatorio")
        private Long tramoId;

        @NotNull(message = "La distancia es obligatoria")
        @Positive(message = "La distancia debe ser positiva")
        private BigDecimal distanciaKm;

        @NotNull(message = "El costo base por km es obligatorio")
        @Positive(message = "El costo base por km debe ser positivo")
        private BigDecimal costoBasePorKm;

        @NotNull(message = "El consumo de combustible es obligatorio")
        @Positive(message = "El consumo de combustible debe ser positivo")
        private BigDecimal consumoKmLitro;
    }
}
