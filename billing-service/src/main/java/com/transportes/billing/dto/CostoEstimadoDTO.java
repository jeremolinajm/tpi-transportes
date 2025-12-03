package com.transportes.billing.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoEstimadoDTO {
    private BigDecimal costoGestion;
    private BigDecimal costoTramos;
    private BigDecimal costoKilometros;
    private BigDecimal costoCombustible;
    private BigDecimal costoEstadia;
    private BigDecimal costoTotal;
}
