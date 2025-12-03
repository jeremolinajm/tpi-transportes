package com.transportes.clients.controller;

import com.transportes.clients.dto.ContenedorDTO;
import com.transportes.clients.entity.Contenedor;
import com.transportes.clients.entity.Solicitud;
import com.transportes.clients.repository.SolicitudRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contenedores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OPERADOR')")
@Tag(name = "Contenedores", description = "Gestión de contenedores")
public class ContenedorController {

    private final SolicitudRepository solicitudRepository;

    @GetMapping("/pendientes")
    @Operation(summary = "Consultar contenedores pendientes con filtros")
    public ResponseEntity<List<ContenedorDTO>> obtenerPendientes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long depositoId) {

        List<Solicitud> solicitudes;

        if (estado != null) {
            try {
                Solicitud.EstadoSolicitud estadoEnum = Solicitud.EstadoSolicitud.valueOf(estado.toUpperCase());
                solicitudes = solicitudRepository.findByEstado(estadoEnum);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            // Estados pendientes: PROGRAMADA, EN_TRANSITO
            solicitudes = solicitudRepository.findPendientes();
        }

        List<ContenedorDTO> contenedores = solicitudes.stream()
                .map(Solicitud::getContenedor)
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contenedor por ID")
    public ResponseEntity<ContenedorDTO> obtenerPorId(@PathVariable Long id) {
        return solicitudRepository.findByContenedorId(id)
                .map(Solicitud::getContenedor)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private ContenedorDTO convertirADTO(Contenedor contenedor) {
        return ContenedorDTO.builder()
                .id(contenedor.getId())
                .codigo(contenedor.getCodigo())
                .pesoKg(contenedor.getPesoKg())
                .volumenM3(contenedor.getVolumenM3())
                .altoM(contenedor.getAltoM())
                .anchoM(contenedor.getAnchoM())
                .largoM(contenedor.getLargoM())
                .descripcion(contenedor.getDescripcion())
                .clienteId(contenedor.getCliente().getId())
                .build();
    }
}
