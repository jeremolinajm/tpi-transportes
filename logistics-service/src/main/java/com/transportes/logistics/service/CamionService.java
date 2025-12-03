package com.transportes.logistics.service;

import com.transportes.logistics.dto.CamionDTO;
import com.transportes.logistics.entity.Camion;
import com.transportes.logistics.entity.Tramo;
import com.transportes.logistics.repository.CamionRepository;
import com.transportes.logistics.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamionService {

    private final CamionRepository camionRepository;
    private final TramoRepository tramoRepository;

    @Transactional
    public void asignarCamionATramo(Long tramoId, Long camionId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado"));

        Camion camion = camionRepository.findById(camionId)
                .orElseThrow(() -> new RuntimeException("Camión no encontrado"));

        // Validar que el camión esté disponible
        if (camion.getEstado() != Camion.EstadoCamion.DISPONIBLE) {
            throw new RuntimeException("El camión no está disponible");
        }

        // Asignar camión al tramo
        tramo.setCamion(camion);
        tramo.setEstado(Tramo.EstadoTramo.ASIGNADO);

        // Cambiar estado del camión
        camion.setEstado(Camion.EstadoCamion.OCUPADO);

        tramoRepository.save(tramo);
        camionRepository.save(camion);

        log.info("Camión {} asignado al tramo {}", camionId, tramoId);
    }

    @Transactional(readOnly = true)
    public List<CamionDTO> obtenerCamionesDisponibles(BigDecimal pesoRequerido, BigDecimal volumenRequerido) {
        List<Camion> camiones = camionRepository.findCamionesDisponiblesParaContenedor(
                pesoRequerido, volumenRequerido);

        return camiones.stream()
                .map(this::convertirACamionDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CamionDTO> obtenerTodosLosCamiones() {
        return camionRepository.findAll().stream()
                .map(this::convertirACamionDTO)
                .collect(Collectors.toList());
    }

    private CamionDTO convertirACamionDTO(Camion camion) {
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
