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

    // DTOs internos
    record CalcularCostoRequest(
        Long solicitudId,
        BigDecimal pesoKg,
        BigDecimal volumenM3,
        Integer cantidadTramos,
        BigDecimal distanciaTotalKm,
        Integer diasEstadiaEstimados,
        List<TramoRequest> tramos
    ) {}

    record TramoRequest(
        Long tramoId,
        BigDecimal distanciaKm,
        BigDecimal costoBaseCamion,
        BigDecimal consumoCombustibleKm,
        BigDecimal horasEstadia
    ) {}

    record CostoEstimadoResponse(
        BigDecimal costoTotal,
        BigDecimal costoGestion,
        BigDecimal costoTransporte,
        BigDecimal costoCombustible,
        BigDecimal costoEstadia
    ) {}
}
