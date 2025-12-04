package com.transportes.logistics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "billing-service", url = "${billing.service.url}")
public interface BillingClient {

    @PostMapping("/api/costos/calcular-estimado")
    CostoEstimadoResponse calcularCostoEstimado(@RequestBody CalcularCostoRequest request);

    @PostMapping("/api/costos/calcular-real")
    CostoEstimadoResponse calcularCostoReal(@RequestBody CalcularCostoRequest request);

    // DTOs internos (deben coincidir con billing-service)
    record CalcularCostoRequest(
        Long solicitudId,
        List<TramoRequest> tramos,
        BigDecimal pesoTotalKg,
        BigDecimal volumenTotalM3,
        Integer diasEstadiaEstimados,
        BigDecimal horasEstadiaTotales,
        BigDecimal costosAdicionales
    ) {}

    record TramoRequest(
        Long tramoId,
        BigDecimal distanciaKm,
        BigDecimal costoBasePorKm,
        BigDecimal consumoKmLitro
    ) {}

    record CostoEstimadoResponse(
        BigDecimal costoTotal,
        BigDecimal costoGestion,
        BigDecimal costoTransporte,
        BigDecimal costoCombustible,
        BigDecimal costoEstadia
    ) {}
}
