package com.transportes.clients.controller;

import com.transportes.clients.client.LogisticsClient;
import com.transportes.clients.dto.CrearSolicitudRequest;
import com.transportes.clients.dto.SeguimientoDTO;
import com.transportes.clients.dto.SolicitudDTO;
import com.transportes.clients.entity.Solicitud;
import com.transportes.clients.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Solicitudes", description = "API para gestión de solicitudes de transporte")
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final LogisticsClient logisticsClient;

    @Operation(summary = "Crear nueva solicitud (con o sin autenticación)")
    @PostMapping
    public ResponseEntity<SolicitudDTO> crearSolicitud(
            @Valid @RequestBody CrearSolicitudRequest request,
            Authentication authentication) {

        String userEmail = null;

        // Si está autenticado, obtener email del token
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                userEmail = jwt.getClaimAsString("email");
                if (userEmail == null) userEmail = authentication.getName();
            } else {
                userEmail = authentication.getName();
            }
        }

        SolicitudDTO solicitud = solicitudService.crearSolicitud(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }

    @Operation(summary = "Obtener solicitud por ID")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDTO> obtenerSolicitud(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String userEmail;
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            userEmail = jwt.getClaimAsString("email");
            if (userEmail == null) userEmail = authentication.getName();
        } else {
            userEmail = authentication.getName();
        }

        SolicitudDTO solicitud = solicitudService.obtenerPorId(id, userEmail);
        return ResponseEntity.ok(solicitud);
    }


    @Operation(summary = "Obtener solicitud por ID (uso interno)")
    @GetMapping("/internal/{id}")
    public ResponseEntity<SolicitudDTO> obtenerSolicitudInternal(@PathVariable("id") Long id) {
        SolicitudDTO solicitud = solicitudService.obtenerPorIdInternal(id);
        return ResponseEntity.ok(solicitud);
    }


    @Operation(summary = "Obtener mis solicitudes")
    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<SolicitudDTO>> obtenerMisSolicitudes(Authentication authentication) {
        String userEmail;
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            userEmail = jwt.getClaimAsString("email");
            if (userEmail == null) userEmail = authentication.getName();
        } else {
            userEmail = authentication.getName();
        }
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesDelCliente(userEmail);
        return ResponseEntity.ok(solicitudes);
    }

    @Operation(summary = "Obtener todas las solicitudes")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping
    public ResponseEntity<List<SolicitudDTO>> obtenerTodasLasSolicitudes() {
        return ResponseEntity.ok(solicitudService.obtenerTodasLasSolicitudes());
    }

    @Operation(summary = "Obtener solicitudes pendientes")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPendientes() {
        return ResponseEntity.ok(solicitudService.obtenerSolicitudesPendientes());
    }

    @Operation(summary = "Obtener seguimiento de solicitud")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @GetMapping("/{id}/seguimiento")
    public ResponseEntity<SeguimientoDTO> obtenerSeguimiento(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String userEmail;
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            userEmail = jwt.getClaimAsString("email");
            if (userEmail == null) userEmail = authentication.getName();
        } else {
            userEmail = authentication.getName();
        }

        SeguimientoDTO seguimiento = solicitudService.obtenerSeguimiento(id, userEmail);
        return ResponseEntity.ok(seguimiento);
    }

    @GetMapping("/{id}/rutas-alternativas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @Operation(summary = "Obtener rutas alternativas para solicitud")
    public ResponseEntity<List<LogisticsClient.RutaDTO>> obtenerRutasAlternativas(@PathVariable("id") Long id) {
        return ResponseEntity.ok(logisticsClient.generarRutasAlternativas(id));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar estado de solicitud")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable("id") Long id,
            @RequestBody ActualizarEstadoRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        Solicitud.EstadoSolicitud estado = Solicitud.EstadoSolicitud.valueOf(request.estado());
        solicitudService.actualizarEstado(id, estado, request.observacion(), userEmail);
        return ResponseEntity.ok().build();
    }

    record ActualizarEstadoRequest(String estado, String observacion) {}
}