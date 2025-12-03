package com.transportes.billing.controller;

import com.transportes.billing.dto.TarifaBaseDTO;
import com.transportes.billing.entity.TarifaBase;
import com.transportes.billing.repository.TarifaBaseRepository;
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
@RequestMapping("/api/tarifas/base")
@PreAuthorize("hasRole('OPERADOR')")
@RequiredArgsConstructor
@Tag(name = "Tarifas Base", description = "Gestión de tarifas base")
public class TarifaBaseController {

    private final TarifaBaseRepository tarifaBaseRepository;

    @GetMapping
    @Operation(summary = "Listar todas las tarifas base")
    public ResponseEntity<List<TarifaBaseDTO>> listarTodas() {
        List<TarifaBaseDTO> tarifas = tarifaBaseRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarifa base por ID")
    public ResponseEntity<TarifaBaseDTO> obtenerPorId(@PathVariable Long id) {
        return tarifaBaseRepository.findById(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vigente")
    @Operation(summary = "Obtener tarifa base vigente")
    public ResponseEntity<TarifaBaseDTO> obtenerVigente() {
        return tarifaBaseRepository.findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc()
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva tarifa base")
    public ResponseEntity<TarifaBaseDTO> crear(@Valid @RequestBody TarifaBaseDTO dto) {
        TarifaBase tarifa = TarifaBase.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .costoFijoGestion(dto.getCostoFijoGestion())
                .costoAdicionalPorTramo(dto.getCostoAdicionalPorTramo())
                .fechaVigenciaDesde(dto.getFechaVigenciaDesde())
                .fechaVigenciaHasta(dto.getFechaVigenciaHasta())
                .activa(dto.getActiva() != null ? dto.getActiva() : true)
                .build();

        TarifaBase guardada = tarifaBaseRepository.save(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa base")
    public ResponseEntity<TarifaBaseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarifaBaseDTO dto) {

        return tarifaBaseRepository.findById(id)
                .map(tarifa -> {
                    tarifa.setNombre(dto.getNombre());
                    tarifa.setDescripcion(dto.getDescripcion());
                    tarifa.setCostoFijoGestion(dto.getCostoFijoGestion());
                    tarifa.setCostoAdicionalPorTramo(dto.getCostoAdicionalPorTramo());
                    tarifa.setFechaVigenciaDesde(dto.getFechaVigenciaDesde());
                    tarifa.setFechaVigenciaHasta(dto.getFechaVigenciaHasta());
                    if (dto.getActiva() != null) {
                        tarifa.setActiva(dto.getActiva());
                    }
                    TarifaBase actualizada = tarifaBaseRepository.save(tarifa);
                    return ResponseEntity.ok(convertirADTO(actualizada));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarifa base")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!tarifaBaseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tarifaBaseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TarifaBaseDTO convertirADTO(TarifaBase tarifa) {
        return TarifaBaseDTO.builder()
                .id(tarifa.getId())
                .nombre(tarifa.getNombre())
                .descripcion(tarifa.getDescripcion())
                .costoFijoGestion(tarifa.getCostoFijoGestion())
                .costoAdicionalPorTramo(tarifa.getCostoAdicionalPorTramo())
                .fechaVigenciaDesde(tarifa.getFechaVigenciaDesde())
                .fechaVigenciaHasta(tarifa.getFechaVigenciaHasta())
                .activa(tarifa.getActiva())
                .fechaCreacion(tarifa.getFechaCreacion())
                .build();
    }
}
