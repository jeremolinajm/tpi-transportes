package com.transportes.billing.controller;

import com.transportes.billing.dto.CalcularCostoRequest;
import com.transportes.billing.dto.CostoSolicitudDTO;
import com.transportes.billing.entity.CostoSolicitud;
import com.transportes.billing.entity.CostoTramo;
import com.transportes.billing.service.CostoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Costos", description = "API para cálculo de costos")
@RestController
@RequestMapping("/api/costos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class CostoController {

    private final CostoService costoService;

    @Operation(summary = "Calcular costo estimado de una solicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    @PostMapping("/calcular-estimado")
    public ResponseEntity<CostoSolicitudDTO> calcularCostoEstimado(
            @Valid @RequestBody CalcularCostoRequest request) {

        List<CostoService.TramoInfo> tramosInfo = request.getTramos().stream()
                .map(t -> new CostoService.TramoInfo(
                        t.getTramoId(),
                        t.getDistanciaKm(),
                        t.getCostoBasePorKm(),
                        t.getConsumoKmLitro()
                ))
                .collect(Collectors.toList());

        CostoSolicitud costo = costoService.calcularCostoEstimado(
                request.getSolicitudId(),
                tramosInfo,
                request.getPesoTotalKg(),
                request.getVolumenTotalM3(),
                request.getDiasEstadiaEstimados()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(costo));
    }

    @Operation(summary = "Calcular costo real/final de una solicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    @PostMapping("/calcular-real")
    public ResponseEntity<CostoSolicitudDTO> calcularCostoReal(
            @Valid @RequestBody CalcularCostoRequest request) {

        List<CostoService.TramoInfo> tramosInfo = request.getTramos().stream()
                .map(t -> new CostoService.TramoInfo(
                        t.getTramoId(),
                        t.getDistanciaKm(),
                        t.getCostoBasePorKm(),
                        t.getConsumoKmLitro()
                ))
                .collect(Collectors.toList());

        CostoSolicitud costo = costoService.calcularCostoReal(
                request.getSolicitudId(),
                tramosInfo,
                request.getPesoTotalKg(),
                request.getVolumenTotalM3(),
                request.getHorasEstadiaTotales(),
                request.getCostosAdicionales()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(costo));
    }

    @Operation(summary = "Obtener todos los costos de una solicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<List<CostoSolicitudDTO>> obtenerCostosSolicitud(
            @PathVariable("solicitudId") Long solicitudId) {

        List<CostoSolicitud> costos = costoService.obtenerCostosSolicitud(solicitudId);
        List<CostoSolicitudDTO> costosDTO = costos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(costosDTO);
    }

    @Operation(summary = "Obtener costo estimado de una solicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping("/solicitud/{solicitudId}/estimado")
    public ResponseEntity<CostoSolicitudDTO> obtenerCostoEstimado(
            @PathVariable("solicitudId") Long solicitudId) {

        try {
            CostoSolicitud costo = costoService.obtenerCostoEstimado(solicitudId);
            return ResponseEntity.ok(convertirADTO(costo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Obtener costo final de una solicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    @GetMapping("/solicitud/{solicitudId}/final")
    public ResponseEntity<CostoSolicitudDTO> obtenerCostoFinal(
            @PathVariable("solicitudId") Long solicitudId) {

        try {
            CostoSolicitud costo = costoService.obtenerCostoFinal(solicitudId);
            return ResponseEntity.ok(convertirADTO(costo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Método auxiliar para convertir entidad a DTO
    private CostoSolicitudDTO convertirADTO(CostoSolicitud costo) {
        return CostoSolicitudDTO.builder()
                .id(costo.getId())
                .solicitudId(costo.getSolicitudId())
                .tipo(costo.getTipo().name())
                .costoGestion(costo.getCostoGestion())
                .costoTransporte(costo.getCostoTransporte())
                .costoCombustible(costo.getCostoCombustible())
                .costoEstadia(costo.getCostoEstadia())
                .costoAdicionales(costo.getCostoAdicionales())
                .costoTotal(costo.getCostoTotal())
                .tarifaBaseId(costo.getTarifaBase() != null ? costo.getTarifaBase().getId() : null)
                .tarifaCombustibleId(costo.getTarifaCombustible() != null ? costo.getTarifaCombustible().getId() : null)
                .tarifaEstadiaId(costo.getTarifaEstadia() != null ? costo.getTarifaEstadia().getId() : null)
                .fechaCalculo(costo.getFechaCalculo())
                .observaciones(costo.getObservaciones())
                .costosTramos(costo.getCostosTramos().stream()
                        .map(this::convertirTramoADTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private com.transportes.billing.dto.CostoTramoDTO convertirTramoADTO(CostoTramo tramo) {
        return com.transportes.billing.dto.CostoTramoDTO.builder()
                .id(tramo.getId())
                .tramoId(tramo.getTramoId())
                .tipo(tramo.getTipo().name())
                .distanciaKm(tramo.getDistanciaKm())
                .costoPorKm(tramo.getCostoPorKm())
                .costoCombustible(tramo.getCostoCombustible())
                .costoEstadia(tramo.getCostoEstadia())
                .horasEstadia(tramo.getHorasEstadia())
                .costoTotalTramo(tramo.getCostoTotalTramo())
                .fechaCalculo(tramo.getFechaCalculo())
                .build();
    }
}
