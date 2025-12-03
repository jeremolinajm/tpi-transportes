package com.transportes.billing.controller;

import com.transportes.billing.dto.TarifaEstadiaDTO;
import com.transportes.billing.entity.TarifaEstadia;
import com.transportes.billing.repository.TarifaEstadiaRepository;
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
@RequestMapping("/api/tarifas/estadia")
@PreAuthorize("hasRole('OPERADOR')")
@RequiredArgsConstructor
@Tag(name = "Tarifas Estadía", description = "Gestión de tarifas de estadía")
public class TarifaEstadiaController {

    private final TarifaEstadiaRepository tarifaEstadiaRepository;

    @GetMapping
    @Operation(summary = "Listar todas las tarifas de estadía")
    public ResponseEntity<List<TarifaEstadiaDTO>> listarTodas() {
        List<TarifaEstadiaDTO> tarifas = tarifaEstadiaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarifa de estadía por ID")
    public ResponseEntity<TarifaEstadiaDTO> obtenerPorId(@PathVariable Long id) {
        return tarifaEstadiaRepository.findById(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vigente")
    @Operation(summary = "Obtener tarifa de estadía vigente")
    public ResponseEntity<TarifaEstadiaDTO> obtenerVigente() {
        return tarifaEstadiaRepository.findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc()
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva tarifa de estadía")
    public ResponseEntity<TarifaEstadiaDTO> crear(@Valid @RequestBody TarifaEstadiaDTO dto) {
        TarifaEstadia tarifa = TarifaEstadia.builder()
                .costoPorDia(dto.getCostoPorDia())
                .costoPorHora(dto.getCostoPorHora())
                .fechaVigenciaDesde(dto.getFechaVigenciaDesde())
                .fechaVigenciaHasta(dto.getFechaVigenciaHasta())
                .activa(dto.getActiva() != null ? dto.getActiva() : true)
                .build();

        TarifaEstadia guardada = tarifaEstadiaRepository.save(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa de estadía")
    public ResponseEntity<TarifaEstadiaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarifaEstadiaDTO dto) {

        return tarifaEstadiaRepository.findById(id)
                .map(tarifa -> {
                    tarifa.setCostoPorDia(dto.getCostoPorDia());
                    tarifa.setCostoPorHora(dto.getCostoPorHora());
                    tarifa.setFechaVigenciaDesde(dto.getFechaVigenciaDesde());
                    tarifa.setFechaVigenciaHasta(dto.getFechaVigenciaHasta());
                    if (dto.getActiva() != null) {
                        tarifa.setActiva(dto.getActiva());
                    }
                    TarifaEstadia actualizada = tarifaEstadiaRepository.save(tarifa);
                    return ResponseEntity.ok(convertirADTO(actualizada));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarifa de estadía")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!tarifaEstadiaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tarifaEstadiaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TarifaEstadiaDTO convertirADTO(TarifaEstadia tarifa) {
        return TarifaEstadiaDTO.builder()
                .id(tarifa.getId())
                .costoPorDia(tarifa.getCostoPorDia())
                .costoPorHora(tarifa.getCostoPorHora())
                .fechaVigenciaDesde(tarifa.getFechaVigenciaDesde())
                .fechaVigenciaHasta(tarifa.getFechaVigenciaHasta())
                .activa(tarifa.getActiva())
                .fechaCreacion(tarifa.getFechaCreacion())
                .build();
    }
}
