package com.transportes.logistics.controller;

import com.transportes.logistics.dto.DepositoDTO;
import com.transportes.logistics.entity.Deposito;
import com.transportes.logistics.repository.DepositoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OPERADOR')")
@Tag(name = "Depósitos", description = "Gestión de depósitos")
public class DepositoController {

    private final DepositoRepository depositoRepository;

    @GetMapping
    @Operation(summary = "Listar todos los depósitos")
    public ResponseEntity<List<DepositoDTO>> listarTodos() {
        List<DepositoDTO> depositos = depositoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(depositos);
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar depósitos activos")
    public ResponseEntity<List<DepositoDTO>> listarActivos() {
        List<DepositoDTO> depositos = depositoRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(depositos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener depósito por ID")
    public ResponseEntity<DepositoDTO> obtenerPorId(@PathVariable Long id) {
        return depositoRepository.findById(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo depósito")
    public ResponseEntity<DepositoDTO> crear(@Valid @RequestBody DepositoDTO depositoDTO) {
        Deposito deposito = convertirAEntidad(depositoDTO);
        Deposito guardado = depositoRepository.save(deposito);
        return ResponseEntity.created(URI.create("/api/depositos/" + guardado.getId()))
                .body(convertirADTO(guardado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar depósito")
    public ResponseEntity<DepositoDTO> actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody DepositoDTO depositoDTO) {
        return depositoRepository.findById(id)
                .map(deposito -> {
                    deposito.setNombre(depositoDTO.getNombre());
                    deposito.setDireccion(depositoDTO.getDireccion());
                    deposito.setLatitud(depositoDTO.getLatitud());
                    deposito.setLongitud(depositoDTO.getLongitud());
                    deposito.setCapacidadMaximaContenedores(depositoDTO.getCapacidadMaxima());
                    deposito.setActivo(depositoDTO.getActivo());
                    return ResponseEntity.ok(convertirADTO(depositoRepository.save(deposito)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar depósito (soft delete)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return depositoRepository.findById(id)
                .map(deposito -> {
                    deposito.setActivo(false);
                    depositoRepository.save(deposito);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private DepositoDTO convertirADTO(Deposito deposito) {
        return DepositoDTO.builder()
                .id(deposito.getId())
                .codigo(deposito.getCodigo())
                .nombre(deposito.getNombre())
                .direccion(deposito.getDireccion())
                .latitud(deposito.getLatitud())
                .longitud(deposito.getLongitud())
                .capacidadMaxima(deposito.getCapacidadMaximaContenedores())
                .contenedoresActuales(deposito.getContenedoresActuales())
                .activo(deposito.getActivo())
                .build();
    }

    private Deposito convertirAEntidad(DepositoDTO dto) {
        return Deposito.builder()
                .nombre(dto.getNombre())
                .direccion(dto.getDireccion())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .capacidadMaximaContenedores(dto.getCapacidadMaxima())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }
}
