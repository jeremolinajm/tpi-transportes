package com.transportes.logistics.service;

import com.transportes.logistics.client.BillingClient;
import com.transportes.logistics.client.ClientsClient;
import com.transportes.logistics.client.OsrmClient;
import com.transportes.logistics.dto.RutaDTO;
import com.transportes.logistics.dto.TramoDTO;
import com.transportes.logistics.entity.*;
import com.transportes.logistics.repository.CamionRepository;
import com.transportes.logistics.repository.DepositoRepository;
import com.transportes.logistics.repository.RutaRepository;
import com.transportes.logistics.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RutaService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;
    private final CamionRepository camionRepository;
    private final OsrmClient osrmClient;
    private final BillingClient billingClient;
    private final ClientsClient clientsClient;

    @Transactional
    public List<RutaDTO> generarRutasAlternativas(Long solicitudId,
                                                   BigDecimal origenLat, BigDecimal origenLon,
                                                   BigDecimal destinoLat, BigDecimal destinoLon,
                                                   BigDecimal pesoKg, BigDecimal volumenM3) {
        log.info("Generando rutas alternativas para solicitud {}", solicitudId);

        // Obtener datos reales de la solicitud
        ClientsClient.SolicitudResponse solicitud = clientsClient.obtenerSolicitud(solicitudId);

        // Usar direcciones reales de la solicitud
        String direccionOrigen = solicitud.origen().direccion();
        BigDecimal latOrigen = solicitud.origen().latitud();
        BigDecimal lonOrigen = solicitud.origen().longitud();

        String direccionDestino = solicitud.destino().direccion();
        BigDecimal latDestino = solicitud.destino().latitud();
        BigDecimal lonDestino = solicitud.destino().longitud();

        BigDecimal peso = solicitud.contenedor().pesoKg();
        BigDecimal volumen = solicitud.contenedor().volumenM3();

        List<RutaDTO> rutasAlternativas = new ArrayList<>();

        // Ruta 1: Directa (sin depósitos)
        Ruta rutaDirecta = crearRutaDirecta(solicitudId, latOrigen, lonOrigen, latDestino, lonDestino,
                                             direccionOrigen, direccionDestino);
        calcularYAsignarCostoEstimado(rutaDirecta, peso, volumen);
        // NO guardamos en BD - solo generamos el DTO
        RutaDTO dto1 = convertirARutaDTO(rutaDirecta);
        dto1.setIndice(0); // Índice para identificar esta ruta
        dto1.setDescripcion("Ruta directa sin depósitos");
        rutasAlternativas.add(dto1);

        // Ruta 2: Con 1 depósito intermedio
        List<Deposito> depositosActivos = depositoRepository.findByActivoTrue();
        if (!depositosActivos.isEmpty()) {
            List<Deposito> depositos1 = seleccionarDepositosParaRuta(latOrigen, lonOrigen,
                                                                       latDestino, lonDestino, 1);
            if (!depositos1.isEmpty()) {
                Ruta rutaCon1Deposito = crearRutaConDepositos(solicitudId, latOrigen, lonOrigen,
                                                               latDestino, lonDestino,
                                                               direccionOrigen, direccionDestino,
                                                               depositos1);
                calcularYAsignarCostoEstimado(rutaCon1Deposito, peso, volumen);
                // NO guardamos en BD - solo generamos el DTO
                RutaDTO dto2 = convertirARutaDTO(rutaCon1Deposito);
                dto2.setIndice(1); // Índice para identificar esta ruta
                dto2.setDescripcion("Ruta con 1 depósito intermedio");
                rutasAlternativas.add(dto2);
            }
        }

        // Ruta 3: Con 2 depósitos intermedios
        if (depositosActivos.size() >= 2) {
            List<Deposito> depositos2 = seleccionarDepositosParaRuta(latOrigen, lonOrigen,
                                                                       latDestino, lonDestino, 2);
            if (depositos2.size() == 2) {
                Ruta rutaCon2Depositos = crearRutaConDepositos(solicitudId, latOrigen, lonOrigen,
                                                                latDestino, lonDestino,
                                                                direccionOrigen, direccionDestino,
                                                                depositos2);
                calcularYAsignarCostoEstimado(rutaCon2Depositos, peso, volumen);
                // NO guardamos en BD - solo generamos el DTO
                RutaDTO dto3 = convertirARutaDTO(rutaCon2Depositos);
                dto3.setIndice(2); // Índice para identificar esta ruta
                dto3.setDescripcion("Ruta con 2 depósitos intermedios");
                rutasAlternativas.add(dto3);
            }
        }

        log.info("Se generaron {} rutas alternativas (NO guardadas en BD)", rutasAlternativas.size());
        return rutasAlternativas;
    }

    private Ruta crearRutaDirecta(Long solicitudId, BigDecimal origenLat, BigDecimal origenLon,
                                  BigDecimal destinoLat, BigDecimal destinoLon,
                                  String direccionOrigen, String direccionDestino) {
        // Calcular distancia con OSRM
        OsrmClient.RouteResponse routeInfo = osrmClient.calcularRuta(origenLat, origenLon,
                                                                      destinoLat, destinoLon);

        Ruta ruta = Ruta.builder()
                .solicitudId(solicitudId)
                .tipo(Ruta.TipoRuta.PROPUESTA)
                .cantidadTramos(1)
                .cantidadDepositos(0)
                .distanciaTotalKm(routeInfo.getDistanciaKm())
                .tiempoEstimadoTotalHoras(routeInfo.getDuracionHoras())
                .tramos(new ArrayList<>())
                .build();

        // Crear tramo directo
        Tramo tramo = Tramo.builder()
                .ruta(ruta)
                .numeroOrden(1)
                .tipoTramo(Tramo.TipoTramo.ORIGEN_DESTINO)
                .origenTipo("ORIGEN")
                .origenDireccion(direccionOrigen)
                .origenLatitud(origenLat)
                .origenLongitud(origenLon)
                .destinoTipo("DESTINO")
                .destinoDireccion(direccionDestino)
                .destinoLatitud(destinoLat)
                .destinoLongitud(destinoLon)
                .distanciaKm(routeInfo.getDistanciaKm())
                .estado(Tramo.EstadoTramo.ESTIMADO)
                .build();

        ruta.getTramos().add(tramo);
        return ruta;
    }

    private Ruta crearRutaConDepositos(Long solicitudId, BigDecimal origenLat, BigDecimal origenLon,
                                       BigDecimal destinoLat, BigDecimal destinoLon,
                                       String direccionOrigen, String direccionDestino,
                                       List<Deposito> depositos) {
        List<Tramo> tramos = new ArrayList<>();
        BigDecimal distanciaTotal = BigDecimal.ZERO;
        int tiempoTotal = 0;
        int numeroOrden = 1;

        // Primer tramo: Origen -> Primer Depósito
        Deposito primerDeposito = depositos.get(0);
        OsrmClient.RouteResponse tramo1 = osrmClient.calcularRuta(
                origenLat, origenLon,
                primerDeposito.getLatitud(), primerDeposito.getLongitud());

        Tramo primerTramo = Tramo.builder()
                .numeroOrden(numeroOrden++)
                .tipoTramo(Tramo.TipoTramo.ORIGEN_DEPOSITO)
                .origenTipo("ORIGEN")
                .origenDireccion(direccionOrigen)
                .origenLatitud(origenLat)
                .origenLongitud(origenLon)
                .destinoTipo("DEPOSITO")
                .destinoDireccion(primerDeposito.getDireccion())
                .destinoLatitud(primerDeposito.getLatitud())
                .destinoLongitud(primerDeposito.getLongitud())
                .destinoDepositoId(primerDeposito.getId())
                .distanciaKm(tramo1.getDistanciaKm())
                .estado(Tramo.EstadoTramo.ESTIMADO)
                .build();

        tramos.add(primerTramo);
        distanciaTotal = distanciaTotal.add(tramo1.getDistanciaKm());
        tiempoTotal += tramo1.getDuracionHoras();

        // Tramos entre depósitos (si hay más de 1 depósito)
        for (int i = 0; i < depositos.size() - 1; i++) {
            Deposito depositoOrigen = depositos.get(i);
            Deposito depositoDestino = depositos.get(i + 1);

            OsrmClient.RouteResponse tramoEntreDepositos = osrmClient.calcularRuta(
                    depositoOrigen.getLatitud(), depositoOrigen.getLongitud(),
                    depositoDestino.getLatitud(), depositoDestino.getLongitud());

            Tramo tramoIntermedio = Tramo.builder()
                    .numeroOrden(numeroOrden++)
                    .tipoTramo(Tramo.TipoTramo.DEPOSITO_DEPOSITO)
                    .origenTipo("DEPOSITO")
                    .origenDireccion(depositoOrigen.getDireccion())
                    .origenLatitud(depositoOrigen.getLatitud())
                    .origenLongitud(depositoOrigen.getLongitud())
                    .origenDepositoId(depositoOrigen.getId())
                    .destinoTipo("DEPOSITO")
                    .destinoDireccion(depositoDestino.getDireccion())
                    .destinoLatitud(depositoDestino.getLatitud())
                    .destinoLongitud(depositoDestino.getLongitud())
                    .destinoDepositoId(depositoDestino.getId())
                    .distanciaKm(tramoEntreDepositos.getDistanciaKm())
                    .estado(Tramo.EstadoTramo.ESTIMADO)
                    .build();

            tramos.add(tramoIntermedio);
            distanciaTotal = distanciaTotal.add(tramoEntreDepositos.getDistanciaKm());
            tiempoTotal += tramoEntreDepositos.getDuracionHoras();
        }

        // Último tramo: Último Depósito -> Destino
        Deposito ultimoDeposito = depositos.get(depositos.size() - 1);
        OsrmClient.RouteResponse tramoFinal = osrmClient.calcularRuta(
                ultimoDeposito.getLatitud(), ultimoDeposito.getLongitud(),
                destinoLat, destinoLon);

        Tramo ultimoTramo = Tramo.builder()
                .numeroOrden(numeroOrden)
                .tipoTramo(Tramo.TipoTramo.DEPOSITO_DESTINO)
                .origenTipo("DEPOSITO")
                .origenDireccion(ultimoDeposito.getDireccion())
                .origenLatitud(ultimoDeposito.getLatitud())
                .origenLongitud(ultimoDeposito.getLongitud())
                .origenDepositoId(ultimoDeposito.getId())
                .destinoTipo("DESTINO")
                .destinoDireccion(direccionDestino)
                .destinoLatitud(destinoLat)
                .destinoLongitud(destinoLon)
                .distanciaKm(tramoFinal.getDistanciaKm())
                .estado(Tramo.EstadoTramo.ESTIMADO)
                .build();

        tramos.add(ultimoTramo);
        distanciaTotal = distanciaTotal.add(tramoFinal.getDistanciaKm());
        tiempoTotal += tramoFinal.getDuracionHoras();

        Ruta ruta = Ruta.builder()
                .solicitudId(solicitudId)
                .tipo(Ruta.TipoRuta.PROPUESTA)
                .cantidadTramos(tramos.size())
                .cantidadDepositos(depositos.size())
                .distanciaTotalKm(distanciaTotal)
                .tiempoEstimadoTotalHoras(tiempoTotal)
                .tramos(new ArrayList<>())
                .build();

        tramos.forEach(t -> {
            t.setRuta(ruta);
            ruta.getTramos().add(t);
        });

        return ruta;
    }

    private List<Deposito> seleccionarDepositosParaRuta(BigDecimal latOrigen, BigDecimal lonOrigen,
                                                         BigDecimal latDestino, BigDecimal lonDestino,
                                                         int cantidadDepositos) {
        List<Deposito> depositosActivos = depositoRepository.findByActivoTrue();

        if (depositosActivos.isEmpty() || cantidadDepositos == 0) {
            return Collections.emptyList();
        }

        if (cantidadDepositos == 1) {
            // Punto medio entre origen y destino
            BigDecimal latMedio = latOrigen.add(latDestino).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
            BigDecimal lonMedio = lonOrigen.add(lonDestino).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);

            return depositosActivos.stream()
                    .min(Comparator.comparingDouble(d ->
                            calcularDistanciaHaversine(latMedio, lonMedio, d.getLatitud(), d.getLongitud())))
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }

        if (cantidadDepositos == 2) {
            // Dividir el trayecto en 3 partes iguales
            BigDecimal lat1 = latOrigen.add(latDestino.subtract(latOrigen).multiply(BigDecimal.valueOf(0.33)));
            BigDecimal lon1 = lonOrigen.add(lonDestino.subtract(lonOrigen).multiply(BigDecimal.valueOf(0.33)));

            BigDecimal lat2 = latOrigen.add(latDestino.subtract(latOrigen).multiply(BigDecimal.valueOf(0.66)));
            BigDecimal lon2 = lonOrigen.add(lonDestino.subtract(lonOrigen).multiply(BigDecimal.valueOf(0.66)));

            Deposito deposito1 = depositosActivos.stream()
                    .min(Comparator.comparingDouble(d ->
                            calcularDistanciaHaversine(lat1, lon1, d.getLatitud(), d.getLongitud())))
                    .orElse(null);

            if (deposito1 == null) {
                return Collections.emptyList();
            }

            List<Deposito> depositosRestantes = depositosActivos.stream()
                    .filter(d -> !d.equals(deposito1))
                    .toList();

            Deposito deposito2 = depositosRestantes.stream()
                    .min(Comparator.comparingDouble(d ->
                            calcularDistanciaHaversine(lat2, lon2, d.getLatitud(), d.getLongitud())))
                    .orElse(null);

            if (deposito2 != null) {
                return List.of(deposito1, deposito2);
            }
        }

        return Collections.emptyList();
    }

    private double calcularDistanciaHaversine(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        final double R = 6371.0; // Radio de la Tierra en kilómetros

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLatRad = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double deltaLonRad = Math.toRadians(lon2.subtract(lon1).doubleValue());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Transactional
    public RutaDTO asignarRuta(Long solicitudId, Integer indiceRuta) {
        log.info("Asignando ruta con índice {} a solicitud {}", indiceRuta, solicitudId);

        // Desmarcar otras rutas de la misma solicitud (si existen)
        List<Ruta> rutasExistentes = rutaRepository.findBySolicitudId(solicitudId);
        rutasExistentes.forEach(r -> {
            r.setSeleccionada(false);
            r.setTipo(Ruta.TipoRuta.PROPUESTA);
        });

        // Obtener datos de la solicitud para regenerar la ruta seleccionada
        ClientsClient.SolicitudResponse solicitud = clientsClient.obtenerSolicitud(solicitudId);

        String direccionOrigen = solicitud.origen().direccion();
        BigDecimal latOrigen = solicitud.origen().latitud();
        BigDecimal lonOrigen = solicitud.origen().longitud();

        String direccionDestino = solicitud.destino().direccion();
        BigDecimal latDestino = solicitud.destino().latitud();
        BigDecimal lonDestino = solicitud.destino().longitud();

        BigDecimal peso = solicitud.contenedor().pesoKg();
        BigDecimal volumen = solicitud.contenedor().volumenM3();

        // Regenerar la ruta según el índice seleccionado
        Ruta rutaSeleccionada;
        String descripcionRuta;

        switch (indiceRuta) {
            case 0: // Ruta directa
                rutaSeleccionada = crearRutaDirecta(solicitudId, latOrigen, lonOrigen,
                        latDestino, lonDestino, direccionOrigen, direccionDestino);
                descripcionRuta = "Ruta directa sin depósitos";
                break;

            case 1: // Ruta con 1 depósito
                List<Deposito> depositos1 = seleccionarDepositosParaRuta(latOrigen, lonOrigen,
                        latDestino, lonDestino, 1);
                if (depositos1.isEmpty()) {
                    throw new RuntimeException("No se pudieron seleccionar depósitos para esta ruta");
                }
                rutaSeleccionada = crearRutaConDepositos(solicitudId, latOrigen, lonOrigen,
                        latDestino, lonDestino, direccionOrigen, direccionDestino, depositos1);
                descripcionRuta = "Ruta con 1 depósito intermedio";
                break;

            case 2: // Ruta con 2 depósitos
                List<Deposito> depositos2 = seleccionarDepositosParaRuta(latOrigen, lonOrigen,
                        latDestino, lonDestino, 2);
                if (depositos2.size() < 2) {
                    throw new RuntimeException("No se pudieron seleccionar 2 depósitos para esta ruta");
                }
                rutaSeleccionada = crearRutaConDepositos(solicitudId, latOrigen, lonOrigen,
                        latDestino, lonDestino, direccionOrigen, direccionDestino, depositos2);
                descripcionRuta = "Ruta con 2 depósitos intermedios";
                break;

            default:
                throw new RuntimeException("Índice de ruta inválido: " + indiceRuta);
        }

        // Asignar índice y descripción
        rutaSeleccionada.setIndice(indiceRuta);
        rutaSeleccionada.setDescripcion(descripcionRuta);

        // Marcar como seleccionada y asignada
        rutaSeleccionada.setSeleccionada(true);
        rutaSeleccionada.setTipo(Ruta.TipoRuta.ASIGNADA);

        // AHORA SÍ guardamos la ruta en BD
        Ruta rutaGuardada = rutaRepository.save(rutaSeleccionada);
        log.info("Ruta {} guardada y asignada a solicitud {}", rutaGuardada.getId(), solicitudId);

        // Refrescar desde BD para obtener los IDs de los tramos generados
        rutaGuardada = rutaRepository.findById(rutaGuardada.getId())
                .orElseThrow(() -> new RuntimeException("No se pudo recuperar la ruta guardada"));

        log.info("Ruta refrescada - ID: {}, Cantidad de tramos: {}", rutaGuardada.getId(), rutaGuardada.getTramos().size());
        rutaGuardada.getTramos().forEach(tramo ->
            log.info("Tramo - ID: {}, NumeroOrden: {}", tramo.getId(), tramo.getNumeroOrden())
        );

        // Calcular costo estimado (DESPUÉS de guardar para que los tramos tengan IDs)
        calcularYAsignarCostoEstimado(rutaGuardada, peso, volumen);
        rutaGuardada = rutaRepository.save(rutaGuardada); // Guardar nuevamente con el costo

        // Actualizar estado de la solicitud a PROGRAMADA
        try {
            ClientsClient.ActualizarEstadoRequest estadoRequest =
                    new ClientsClient.ActualizarEstadoRequest("PROGRAMADA", "Ruta asignada y programada para transporte");

            clientsClient.actualizarEstado(solicitudId, estadoRequest);
            log.info("Solicitud {} actualizada a estado PROGRAMADA", solicitudId);
        } catch (Exception e) {
            log.error("Error al actualizar estado de solicitud a PROGRAMADA: {}", e.getMessage(), e);
        }

        // Retornar la ruta guardada con los IDs de los tramos
        return convertirARutaDTO(rutaGuardada);
    }

    private RutaDTO convertirARutaDTO(Ruta ruta) {
        List<TramoDTO> tramosDTO = ruta.getTramos().stream()
                .map(tramo -> TramoDTO.builder()
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
                        .build())
                .collect(Collectors.toList());

        return RutaDTO.builder()
                .id(ruta.getId())
                .solicitudId(ruta.getSolicitudId())
                .tipo(ruta.getTipo().name())
                .indice(ruta.getIndice())
                .descripcion(ruta.getDescripcion())
                .cantidadTramos(ruta.getCantidadTramos())
                .cantidadDepositos(ruta.getCantidadDepositos())
                .distanciaTotalKm(ruta.getDistanciaTotalKm())
                .tiempoEstimadoTotalHoras(ruta.getTiempoEstimadoTotalHoras())
                .costoEstimadoTotal(ruta.getCostoEstimadoTotal())
                .seleccionada(ruta.getSeleccionada())
                .tramos(tramosDTO)
                .build();
    }

    private void calcularYAsignarCostoEstimado(Ruta ruta, BigDecimal pesoKg, BigDecimal volumenM3) {
        try {
            // Obtener un camión disponible para obtener costos base
            Camion camion = camionRepository.findAll().stream()
                    .filter(c -> c.getActivo() && c.getEstado() == Camion.EstadoCamion.DISPONIBLE)
                    .findFirst()
                    .orElse(null);

            if (camion == null) {
                log.warn("No hay camiones disponibles para calcular costo estimado");
                return;
            }

            // Construir request para BillingClient
            List<BillingClient.TramoRequest> tramosRequest = ruta.getTramos().stream()
                    .map(tramo -> new BillingClient.TramoRequest(
                            tramo.getId(),
                            tramo.getDistanciaKm(),
                            camion.getCostoBasePorKm(),
                            camion.getConsumoCombustibleKmLitro()
                    ))
                    .collect(Collectors.toList());

            // Estimar días de estadía (1 día por cada depósito)
            Integer diasEstadia = ruta.getCantidadDepositos();

            BillingClient.CalcularCostoRequest request = new BillingClient.CalcularCostoRequest(
                    ruta.getSolicitudId(),
                    tramosRequest,
                    pesoKg,
                    volumenM3,
                    diasEstadia,
                    null, // horasEstadiaTotales para cálculo estimado
                    null  // costosAdicionales para cálculo estimado
            );

            // Llamar a billing-service
            BillingClient.CostoEstimadoResponse costoResponse = billingClient.calcularCostoEstimado(request);

            // Asignar costo estimado a la ruta
            ruta.setCostoEstimadoTotal(costoResponse.costoTotal());

            log.info("Costo estimado calculado para ruta {}: {}", ruta.getId(), costoResponse.costoTotal());
        } catch (Exception e) {
            log.error("Error al calcular costo estimado para ruta {}: {}", ruta.getId(), e.getMessage());
        }
    }
}
