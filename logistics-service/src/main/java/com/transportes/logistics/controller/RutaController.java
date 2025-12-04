package com.transportes.logistics.controller;

import com.transportes.logistics.client.ClientsClient;
import com.transportes.logistics.dto.RutaDTO;
import com.transportes.logistics.service.RutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Rutas", description = "API para gestión de rutas de transporte")
@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class RutaController {

    private final RutaService rutaService;
    private final ClientsClient clientsClient;

    @Operation(summary = "Generar rutas alternativas", description = "Genera múltiples opciones de ruta para una solicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping("/alternativas/{solicitudId}")
    public ResponseEntity<List<RutaDTO>> generarRutasAlternativas(@PathVariable("solicitudId") Long solicitudId) {
        // Obtener información de la solicitud desde clients-service
        ClientsClient.SolicitudResponse solicitud = clientsClient.obtenerSolicitud(solicitudId);

        // Extraer datos necesarios
        BigDecimal origenLat = solicitud.origen().latitud();
        BigDecimal origenLon = solicitud.origen().longitud();
        BigDecimal destinoLat = solicitud.destino().latitud();
        BigDecimal destinoLon = solicitud.destino().longitud();
        BigDecimal pesoKg = solicitud.contenedor().pesoKg();
        BigDecimal volumenM3 = solicitud.contenedor().volumenM3();

        List<RutaDTO> rutas = rutaService.generarRutasAlternativas(
                solicitudId, origenLat, origenLon, destinoLat, destinoLon, pesoKg, volumenM3);

        return ResponseEntity.ok(rutas);
    }

    @Operation(summary = "Asignar ruta a solicitud",
               description = "Regenera y guarda la ruta seleccionada según su índice (0=directa, 1=1 depósito, 2=2 depósitos)")
    @PreAuthorize("hasRole('OPERADOR')")
    @PostMapping("/{solicitudId}/asignar/{indiceRuta}")
    public ResponseEntity<RutaDTO> asignarRuta(
            @PathVariable("solicitudId") Long solicitudId,
            @PathVariable("indiceRuta") Integer indiceRuta) {
        RutaDTO ruta = rutaService.asignarRuta(solicitudId, indiceRuta);
        return ResponseEntity.ok(ruta);
    }
}
