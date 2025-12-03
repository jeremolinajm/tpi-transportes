package com.transportes.logistics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "clients-service", url = "${clients.service.url}")
public interface ClientsClient {

    @PutMapping("/api/solicitudes/{id}/estado")
    void actualizarEstado(@PathVariable("id") Long id, @RequestBody ActualizarEstadoRequest request);

    @GetMapping("/api/solicitudes/internal/{id}")
    SolicitudResponse obtenerSolicitud(@PathVariable("id") Long id);

    record ActualizarEstadoRequest(
        String estado,
        String observacion
    ) {}

    record SolicitudResponse(
        Long id,
        String numeroSolicitud,
        String estado,
        ContenedorData contenedor,
        UbicacionData origen,
        UbicacionData destino
    ) {}

    record ContenedorData(
        Long id,
        java.math.BigDecimal pesoKg,
        java.math.BigDecimal volumenM3
    ) {}

    record UbicacionData(
        Long id,
        String direccion,
        java.math.BigDecimal latitud,
        java.math.BigDecimal longitud
    ) {}
}
