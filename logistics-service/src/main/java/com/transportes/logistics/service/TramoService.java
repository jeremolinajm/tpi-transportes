package com.transportes.logistics.service;

import com.transportes.logistics.client.BillingClient;
import com.transportes.logistics.client.ClientsClient;
import com.transportes.logistics.dto.TramoDTO;
import com.transportes.logistics.entity.Camion;
import com.transportes.logistics.entity.Ruta;
import com.transportes.logistics.entity.Tramo;
import com.transportes.logistics.repository.CamionRepository;
import com.transportes.logistics.repository.RutaRepository;
import com.transportes.logistics.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TramoService {

    private final TramoRepository tramoRepository;
    private final CamionRepository camionRepository;
    private final RutaRepository rutaRepository;
    private final ClientsClient clientsClient;
    private final BillingClient billingClient;

    @Transactional(readOnly = true)
    public List<TramoDTO> obtenerTramosAsignadosATransportista(String keycloakUserId) {
        List<Tramo> tramos = tramoRepository.findTramosAsignadosPorTransportista(keycloakUserId);

        return tramos.stream()
                .map(this::convertirATramoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TramoDTO iniciarTramo(Long tramoId, String keycloakUserId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado"));

        // Validar que el tramo esté asignado
        if (tramo.getEstado() != Tramo.EstadoTramo.ASIGNADO) {
            throw new RuntimeException("El tramo no está en estado ASIGNADO");
        }

        // Validar que el transportista sea el asignado
        if (tramo.getCamion() == null || tramo.getCamion().getTransportista() == null ||
            !tramo.getCamion().getTransportista().getKeycloakUserId().equals(keycloakUserId)) {
            throw new RuntimeException("No tiene permisos para iniciar este tramo");
        }

        // Marcar inicio
        tramo.setEstado(Tramo.EstadoTramo.INICIADO);
        tramo.setFechaHoraInicioReal(LocalDateTime.now());

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo {} iniciado por transportista", tramoId);

        // Verificar si es el primer tramo de la ruta y actualizar estado de solicitud
        verificarYActualizarEstadoEnTransito(tramoActualizado);

        return convertirATramoDTO(tramoActualizado);
    }

    @Transactional
    public TramoDTO finalizarTramo(Long tramoId, String keycloakUserId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado"));

        // Validar que el tramo esté iniciado
        if (tramo.getEstado() != Tramo.EstadoTramo.INICIADO) {
            throw new RuntimeException("El tramo no está en estado INICIADO");
        }

        // Validar que el transportista sea el asignado
        if (tramo.getCamion() == null || tramo.getCamion().getTransportista() == null ||
            !tramo.getCamion().getTransportista().getKeycloakUserId().equals(keycloakUserId)) {
            throw new RuntimeException("No tiene permisos para finalizar este tramo");
        }

        // Marcar fin
        tramo.setEstado(Tramo.EstadoTramo.FINALIZADO);
        tramo.setFechaHoraFinReal(LocalDateTime.now());

        // Liberar camión si no hay más tramos pendientes
        Camion camion = tramo.getCamion();
        List<Tramo> tramosRestantes = tramoRepository.findByCamionId(camion.getId()).stream()
                .filter(t -> t.getEstado() == Tramo.EstadoTramo.ASIGNADO || t.getEstado() == Tramo.EstadoTramo.INICIADO)
                .collect(Collectors.toList());

        if (tramosRestantes.isEmpty()) {
            camion.setEstado(Camion.EstadoCamion.DISPONIBLE);
            camionRepository.save(camion);
            log.info("Camión {} liberado", camion.getId());
        }

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo {} finalizado por transportista", tramoId);

        // Verificar si es el último tramo de la ruta
        verificarYFinalizarRuta(tramoActualizado);

        return convertirATramoDTO(tramoActualizado);
    }

    @Transactional(readOnly = true)
    public TramoDTO obtenerTramo(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado"));
        return convertirATramoDTO(tramo);
    }

    private TramoDTO convertirATramoDTO(Tramo tramo) {
        return TramoDTO.builder()
                .id(tramo.getId())
                .numeroOrden(tramo.getNumeroOrden())
                .tipoTramo(tramo.getTipoTramo())
                .origenDireccion(tramo.getOrigenDireccion())
                .destinoDireccion(tramo.getDestinoDireccion())
                .distanciaKm(tramo.getDistanciaKm())
                .estado(tramo.getEstado())
                .camionId(tramo.getCamion() != null ? tramo.getCamion().getId() : null)
                .camionDominio(tramo.getCamion() != null ? tramo.getCamion().getDominio() : null)
                .fechaHoraInicioEstimada(tramo.getFechaHoraInicioEstimada())
                .fechaHoraFinEstimada(tramo.getFechaHoraFinEstimada())
                .fechaHoraInicioReal(tramo.getFechaHoraInicioReal())
                .fechaHoraFinReal(tramo.getFechaHoraFinReal())
                .costoEstimado(tramo.getCostoEstimado())
                .costoReal(tramo.getCostoReal())
                .build();
    }

    private void verificarYActualizarEstadoEnTransito(Tramo tramoIniciado) {
        try {
            Ruta ruta = tramoIniciado.getRuta();

            // Verificar si es el primer tramo en iniciarse (numeroOrden = 1)
            if (tramoIniciado.getNumeroOrden() == 1) {
                log.info("Primer tramo de la ruta {} iniciado. Actualizando solicitud a EN_TRANSITO...", ruta.getId());

                // Actualizar estado de la solicitud a EN_TRANSITO
                ClientsClient.ActualizarEstadoRequest estadoRequest =
                        new ClientsClient.ActualizarEstadoRequest("EN_TRANSITO", "Transporte iniciado");

                clientsClient.actualizarEstado(ruta.getSolicitudId(), estadoRequest);

                log.info("Solicitud {} actualizada a estado EN_TRANSITO", ruta.getSolicitudId());
            }
        } catch (Exception e) {
            log.error("Error al actualizar estado de solicitud a EN_TRANSITO: {}", e.getMessage(), e);
        }
    }

    private void verificarYFinalizarRuta(Tramo tramoFinalizado) {
        try {
            Ruta ruta = tramoFinalizado.getRuta();

            // Verificar si todos los tramos de la ruta están finalizados
            boolean todosFinalizados = ruta.getTramos().stream()
                    .allMatch(t -> t.getEstado() == Tramo.EstadoTramo.FINALIZADO);

            if (todosFinalizados) {
                log.info("Todos los tramos de la ruta {} finalizados. Procesando finalización...", ruta.getId());

                // Obtener datos de la solicitud
                ClientsClient.SolicitudResponse solicitud = clientsClient.obtenerSolicitud(ruta.getSolicitudId());

                // Calcular costo real
                List<BillingClient.TramoRequest> tramosRequest = ruta.getTramos().stream()
                        .map(tramo -> {
                            Camion camion = tramo.getCamion();
                            return new BillingClient.TramoRequest(
                                    tramo.getId(),
                                    tramo.getDistanciaKm(),
                                    camion.getCostoBasePorKm(),
                                    camion.getConsumoCombustibleKmLitro(),
                                    BigDecimal.ZERO // horasEstadia reales (pendiente implementar)
                            );
                        })
                        .collect(Collectors.toList());

                BillingClient.CalcularCostoRequest costoRequest = new BillingClient.CalcularCostoRequest(
                        ruta.getSolicitudId(),
                        solicitud.contenedor().pesoKg(),
                        solicitud.contenedor().volumenM3(),
                        ruta.getCantidadTramos(),
                        ruta.getDistanciaTotalKm(),
                        ruta.getCantidadDepositos(), // días de estadía
                        tramosRequest
                );

                BillingClient.CostoEstimadoResponse costoReal = billingClient.calcularCostoReal(costoRequest);

                // Actualizar costo real en la ruta
                ruta.setCostoRealTotal(costoReal.costoTotal());
                rutaRepository.save(ruta);

                log.info("Costo real calculado para ruta {}: {}", ruta.getId(), costoReal.costoTotal());

                // Actualizar estado de la solicitud a ENTREGADA
                ClientsClient.ActualizarEstadoRequest estadoRequest =
                        new ClientsClient.ActualizarEstadoRequest("ENTREGADA", "Entrega completada exitosamente");

                clientsClient.actualizarEstado(ruta.getSolicitudId(), estadoRequest);

                log.info("Solicitud {} actualizada a estado ENTREGADA", ruta.getSolicitudId());
            }
        } catch (Exception e) {
            log.error("Error al verificar y finalizar ruta: {}", e.getMessage(), e);
        }
    }
}
