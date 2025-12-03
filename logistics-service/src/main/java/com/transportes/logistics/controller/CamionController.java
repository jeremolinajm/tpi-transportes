package com.transportes.logistics.controller;

import com.transportes.logistics.dto.CamionDTO;
import com.transportes.logistics.entity.Camion;
import com.transportes.logistics.entity.Transportista;
import com.transportes.logistics.repository.CamionRepository;
import com.transportes.logistics.repository.TransportistaRepository;
import com.transportes.logistics.service.CamionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@Tag(name = "Camiones", description = "API para gestión de camiones")
@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class CamionController {

    private final CamionService camionService;
    private final CamionRepository camionRepository;
    private final TransportistaRepository transportistaRepository;

    @Operation(summary = "Obtener todos los camiones")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping
    public ResponseEntity<List<CamionDTO>> obtenerTodos() {
        List<CamionDTO> camiones = camionService.obtenerTodosLosCamiones();
        return ResponseEntity.ok(camiones);
    }

    @Operation(summary = "Obtener camiones disponibles", description = "Filtra por capacidad de peso y volumen")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping("/disponibles")
    public ResponseEntity<List<CamionDTO>> obtenerDisponibles(
            @RequestParam BigDecimal pesoKg,
            @RequestParam BigDecimal volumenM3) {

        List<CamionDTO> camiones = camionService.obtenerCamionesDisponibles(pesoKg, volumenM3);
        return ResponseEntity.ok(camiones);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear nuevo camión")
    public ResponseEntity<CamionDTO> crear(@Valid @RequestBody CamionDTO camionDTO) {
        // Buscar transportista si se proporciona
        Transportista transportista = null;
        if (camionDTO.getTransportistaId() != null) {
            transportista = transportistaRepository.findById(camionDTO.getTransportistaId())
                    .orElseThrow(() -> new RuntimeException("Transportista no encontrado"));
        }

        Camion camion = Camion.builder()
                .dominio(camionDTO.getDominio())
                .marca(camionDTO.getMarca())
                .modelo(camionDTO.getModelo())
                .anio(camionDTO.getAnio())
                .capacidadPesoKg(camionDTO.getCapacidadPesoKg())
                .capacidadVolumenM3(camionDTO.getCapacidadVolumenM3())
                .consumoCombustibleKmLitro(camionDTO.getConsumoCombustibleKmLitro())
                .costoBasePorKm(camionDTO.getCostoBasePorKm())
                .transportista(transportista)
                .estado(camionDTO.getEstado() != null ?
                        camionDTO.getEstado() :
                        Camion.EstadoCamion.DISPONIBLE)
                .activo(camionDTO.getActivo() != null ? camionDTO.getActivo() : true)
                .build();

        Camion guardado = camionRepository.save(camion);
        return ResponseEntity.created(URI.create("/api/camiones/" + guardado.getId()))
                .body(convertirADTO(guardado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar camión")
    public ResponseEntity<CamionDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CamionDTO camionDTO) {
        return camionRepository.findById(id)
                .map(camion -> {
                    // Actualizar transportista si se proporciona
                    if (camionDTO.getTransportistaId() != null) {
                        Transportista transportista = transportistaRepository.findById(camionDTO.getTransportistaId())
                                .orElseThrow(() -> new RuntimeException("Transportista no encontrado"));
                        camion.setTransportista(transportista);
                    }

                    // Actualizar datos del camión
                    if (camionDTO.getDominio() != null) {
                        camion.setDominio(camionDTO.getDominio());
                    }
                    if (camionDTO.getMarca() != null) {
                        camion.setMarca(camionDTO.getMarca());
                    }
                    if (camionDTO.getModelo() != null) {
                        camion.setModelo(camionDTO.getModelo());
                    }
                    if (camionDTO.getAnio() != null) {
                        camion.setAnio(camionDTO.getAnio());
                    }
                    if (camionDTO.getCapacidadPesoKg() != null) {
                        camion.setCapacidadPesoKg(camionDTO.getCapacidadPesoKg());
                    }
                    if (camionDTO.getCapacidadVolumenM3() != null) {
                        camion.setCapacidadVolumenM3(camionDTO.getCapacidadVolumenM3());
                    }
                    if (camionDTO.getConsumoCombustibleKmLitro() != null) {
                        camion.setConsumoCombustibleKmLitro(camionDTO.getConsumoCombustibleKmLitro());
                    }
                    if (camionDTO.getCostoBasePorKm() != null) {
                        camion.setCostoBasePorKm(camionDTO.getCostoBasePorKm());
                    }
                    if (camionDTO.getEstado() != null) {
                        camion.setEstado(camionDTO.getEstado());
                    }
                    if (camionDTO.getActivo() != null) {
                        camion.setActivo(camionDTO.getActivo());
                    }

                    return ResponseEntity.ok(convertirADTO(camionRepository.save(camion)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar camión (soft delete)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return camionRepository.findById(id)
                .map(camion -> {
                    camion.setActivo(false);
                    camion.setEstado(Camion.EstadoCamion.INACTIVO);
                    camionRepository.save(camion);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private CamionDTO convertirADTO(Camion camion) {
        return CamionDTO.builder()
                .id(camion.getId())
                .dominio(camion.getDominio())
                .marca(camion.getMarca())
                .modelo(camion.getModelo())
                .anio(camion.getAnio())
                .capacidadPesoKg(camion.getCapacidadPesoKg())
                .capacidadVolumenM3(camion.getCapacidadVolumenM3())
                .consumoCombustibleKmLitro(camion.getConsumoCombustibleKmLitro())
                .costoBasePorKm(camion.getCostoBasePorKm())
                .transportistaId(camion.getTransportista() != null ? camion.getTransportista().getId() : null)
                .estado(camion.getEstado())
                .activo(camion.getActivo())
                .build();
    }
}
