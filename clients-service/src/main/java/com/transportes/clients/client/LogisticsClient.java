package com.transportes.clients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "logistics-service", url = "${logistics.service.url}")
public interface LogisticsClient {

    @GetMapping("/api/rutas/alternativas/{solicitudId}")
    List<RutaDTO> generarRutasAlternativas(@PathVariable("solicitudId") Long solicitudId);

    record RutaDTO(
        Long id,
        Long solicitudId,
        String tipo,
        Integer cantidadTramos,
        Integer cantidadDepositos,
        BigDecimal distanciaTotalKm,
        Integer tiempoEstimadoHoras,
        BigDecimal costoEstimadoTotal,
        List<TramoDTO> tramos
    ) {}

    record TramoDTO(
        Long id,
        String tipoTramo,
        BigDecimal distanciaKm,
        String estado
    ) {}
}
