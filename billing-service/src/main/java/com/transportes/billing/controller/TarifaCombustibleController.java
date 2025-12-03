package com.transportes.billing.controller;

import com.transportes.billing.dto.TarifaCombustibleDTO;
import com.transportes.billing.entity.TarifaCombustible;
import com.transportes.billing.repository.TarifaCombustibleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tarifas/combustible")
@PreAuthorize("hasRole('OPERADOR')")
@RequiredArgsConstructor
@Tag(name = "Tarifas Combustible", description = "Gestión de tarifas de combustible")
public class TarifaCombustibleController {

    private final TarifaCombustibleRepository tarifaCombustibleRepository;

    @GetMapping
    @Operation(summary = "Listar todas las tarifas de combustible")
    public ResponseEntity<List<TarifaCombustibleDTO>> listarTodas() {
        List<TarifaCombustibleDTO> tarifas = tarifaCombustibleRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarifa de combustible por ID")
    public ResponseEntity<TarifaCombustibleDTO> obtenerPorId(@PathVariable Long id) {
        return tarifaCombustibleRepository.findById(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vigente")
    @Operation(summary = "Obtener tarifa de combustible vigente")
    public ResponseEntity<TarifaCombustibleDTO> obtenerVigente() {
        return tarifaCombustibleRepository.findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc()
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva tarifa de combustible")
    public ResponseEntity<TarifaCombustibleDTO> crear(@Valid @RequestBody TarifaCombustibleDTO dto) {
        TarifaCombustible tarifa = TarifaCombustible.builder()
                .precioPorLitro(dto.getPrecioPorLitro())
                .fechaVigenciaDesde(dto.getFechaVigenciaDesde())
                .fechaVigenciaHasta(dto.getFechaVigenciaHasta())
                .activa(dto.getActiva() != null ? dto.getActiva() : true)
                .build();

        TarifaCombustible guardada = tarifaCombustibleRepository.save(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa de combustible")
    public ResponseEntity<TarifaCombustibleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarifaCombustibleDTO dto) {

        return tarifaCombustibleRepository.findById(id)
                .map(tarifa -> {
                    tarifa.setPrecioPorLitro(dto.getPrecioPorLitro());
                    tarifa.setFechaVigenciaDesde(dto.getFechaVigenciaDesde());
                    tarifa.setFechaVigenciaHasta(dto.getFechaVigenciaHasta());
                    if (dto.getActiva() != null) {
                        tarifa.setActiva(dto.getActiva());
                    }
                    TarifaCombustible actualizada = tarifaCombustibleRepository.save(tarifa);
                    return ResponseEntity.ok(convertirADTO(actualizada));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarifa de combustible")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!tarifaCombustibleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tarifaCombustibleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TarifaCombustibleDTO convertirADTO(TarifaCombustible tarifa) {
        return TarifaCombustibleDTO.builder()
                .id(tarifa.getId())
                .precioPorLitro(tarifa.getPrecioPorLitro())
                .fechaVigenciaDesde(tarifa.getFechaVigenciaDesde())
                .fechaVigenciaHasta(tarifa.getFechaVigenciaHasta())
                .activa(tarifa.getActiva())
                .fechaCreacion(tarifa.getFechaCreacion())
                .build();
    }
}
