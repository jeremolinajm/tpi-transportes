package com.transportes.logistics.controller;

import com.transportes.logistics.dto.TramoDTO;
import com.transportes.logistics.dto.TransportistaDTO;
import com.transportes.logistics.service.TramoService;
import com.transportes.logistics.service.TransportistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Transportista", description = "API para operaciones de transportistas")
@RestController
@RequestMapping("/api/transportista")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class TransportistaController {

    private final TramoService tramoService;
    private final TransportistaService transportistaService;

    @Operation(summary = "Crear nuevo transportista")
    @PreAuthorize("hasRole('OPERADOR')")
    @PostMapping
    public ResponseEntity<TransportistaDTO> crear(@Valid @RequestBody TransportistaDTO dto) {
        return ResponseEntity.ok(transportistaService.crear(dto));
    }

    @Operation(summary = "Obtener mis tramos asignados")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    @GetMapping("/tramos-asignados")
    public ResponseEntity<List<TramoDTO>> obtenerMisTramosAsignados(Authentication authentication) {
        String keycloakUserId = authentication.getName();
        List<TramoDTO> tramos = tramoService.obtenerTramosAsignadosATransportista(keycloakUserId);
        return ResponseEntity.ok(tramos);
    }

    @Operation(summary = "Iniciar tramo")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    @PostMapping("/tramos/{tramoId}/iniciar")
    public ResponseEntity<TramoDTO> iniciarTramo(
            @PathVariable Long tramoId,
            Authentication authentication) {

        String keycloakUserId = authentication.getName();
        TramoDTO tramo = tramoService.iniciarTramo(tramoId, keycloakUserId);
        return ResponseEntity.ok(tramo);
    }

    @Operation(summary = "Finalizar tramo")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    @PostMapping("/tramos/{tramoId}/finalizar")
    public ResponseEntity<TramoDTO> finalizarTramo(
            @PathVariable Long tramoId,
            Authentication authentication) {

        String keycloakUserId = authentication.getName();
        TramoDTO tramo = tramoService.finalizarTramo(tramoId, keycloakUserId);
        return ResponseEntity.ok(tramo);
    }
}
