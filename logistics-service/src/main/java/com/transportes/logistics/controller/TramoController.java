package com.transportes.logistics.controller;

import com.transportes.logistics.dto.TramoDTO;
import com.transportes.logistics.service.CamionService;
import com.transportes.logistics.service.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Tramos", description = "API para gestión de tramos de ruta")
@RestController
@RequestMapping("/api/tramos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class TramoController {

    private final TramoService tramoService;
    private final CamionService camionService;

    @Operation(summary = "Asignar camión a tramo")
    @PreAuthorize("hasRole('OPERADOR')")
    @PostMapping("/{tramoId}/asignar-camion")
    public ResponseEntity<Void> asignarCamion(
            @PathVariable("tramoId") Long tramoId,
            @RequestParam("camionId") Long camionId) {

        camionService.asignarCamionATramo(tramoId, camionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Obtener tramo por ID")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @GetMapping("/{tramoId}")
    public ResponseEntity<TramoDTO> obtenerTramo(@PathVariable("tramoID") Long tramoId) {
        TramoDTO tramo = tramoService.obtenerTramo(tramoId);
        return ResponseEntity.ok(tramo);
    }
}
