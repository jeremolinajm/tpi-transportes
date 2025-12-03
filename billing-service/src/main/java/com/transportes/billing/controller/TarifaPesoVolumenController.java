package com.transportes.billing.controller;

import com.transportes.billing.dto.TarifaPesoVolumenDTO;
import com.transportes.billing.entity.TarifaPesoVolumen;
import com.transportes.billing.repository.TarifaPesoVolumenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tarifas/peso-volumen")
@PreAuthorize("hasRole('OPERADOR')")
@RequiredArgsConstructor
@Tag(name = "Tarifas Peso/Volumen", description = "Gestión de tarifas por peso y volumen")
public class TarifaPesoVolumenController {

    private final TarifaPesoVolumenRepository tarifaPesoVolumenRepository;

    @GetMapping
    @Operation(summary = "Listar todas las tarifas de peso/volumen")
    public ResponseEntity<List<TarifaPesoVolumenDTO>> listarTodas() {
        List<TarifaPesoVolumenDTO> tarifas = tarifaPesoVolumenRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarifa de peso/volumen por ID")
    public ResponseEntity<TarifaPesoVolumenDTO> obtenerPorId(@PathVariable Long id) {
        return tarifaPesoVolumenRepository.findById(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar tarifa por peso y volumen")
    public ResponseEntity<TarifaPesoVolumenDTO> buscarPorPesoYVolumen(
            @RequestParam BigDecimal peso,
            @RequestParam BigDecimal volumen) {
        return tarifaPesoVolumenRepository.findByPesoYVolumen(peso, volumen)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva tarifa de peso/volumen")
    public ResponseEntity<TarifaPesoVolumenDTO> crear(@Valid @RequestBody TarifaPesoVolumenDTO dto) {
        TarifaPesoVolumen tarifa = TarifaPesoVolumen.builder()
                .pesoMinimoKg(dto.getPesoMinimoKg())
                .pesoMaximoKg(dto.getPesoMaximoKg())
                .volumenMinimoM3(dto.getVolumenMinimoM3())
                .volumenMaximoM3(dto.getVolumenMaximoM3())
                .multiplicadorCosto(dto.getMultiplicadorCosto())
                .fechaVigenciaDesde(dto.getFechaVigenciaDesde())
                .fechaVigenciaHasta(dto.getFechaVigenciaHasta())
                .activa(dto.getActiva() != null ? dto.getActiva() : true)
                .build();

        TarifaPesoVolumen guardada = tarifaPesoVolumenRepository.save(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa de peso/volumen")
    public ResponseEntity<TarifaPesoVolumenDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarifaPesoVolumenDTO dto) {

        return tarifaPesoVolumenRepository.findById(id)
                .map(tarifa -> {
                    tarifa.setPesoMinimoKg(dto.getPesoMinimoKg());
                    tarifa.setPesoMaximoKg(dto.getPesoMaximoKg());
                    tarifa.setVolumenMinimoM3(dto.getVolumenMinimoM3());
                    tarifa.setVolumenMaximoM3(dto.getVolumenMaximoM3());
                    tarifa.setMultiplicadorCosto(dto.getMultiplicadorCosto());
                    tarifa.setFechaVigenciaDesde(dto.getFechaVigenciaDesde());
                    tarifa.setFechaVigenciaHasta(dto.getFechaVigenciaHasta());
                    if (dto.getActiva() != null) {
                        tarifa.setActiva(dto.getActiva());
                    }
                    TarifaPesoVolumen actualizada = tarifaPesoVolumenRepository.save(tarifa);
                    return ResponseEntity.ok(convertirADTO(actualizada));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarifa de peso/volumen")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!tarifaPesoVolumenRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tarifaPesoVolumenRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TarifaPesoVolumenDTO convertirADTO(TarifaPesoVolumen tarifa) {
        return TarifaPesoVolumenDTO.builder()
                .id(tarifa.getId())
                .pesoMinimoKg(tarifa.getPesoMinimoKg())
                .pesoMaximoKg(tarifa.getPesoMaximoKg())
                .volumenMinimoM3(tarifa.getVolumenMinimoM3())
                .volumenMaximoM3(tarifa.getVolumenMaximoM3())
                .multiplicadorCosto(tarifa.getMultiplicadorCosto())
                .fechaVigenciaDesde(tarifa.getFechaVigenciaDesde())
                .fechaVigenciaHasta(tarifa.getFechaVigenciaHasta())
                .activa(tarifa.getActiva())
                .fechaCreacion(tarifa.getFechaCreacion())
                .build();
    }
}
