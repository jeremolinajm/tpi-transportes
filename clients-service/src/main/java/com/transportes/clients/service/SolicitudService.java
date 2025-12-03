package com.transportes.clients.service;

import com.transportes.clients.dto.*;
import com.transportes.clients.entity.*;
import com.transportes.clients.exception.BusinessException;
import com.transportes.clients.exception.ResourceNotFoundException;
import com.transportes.clients.mapper.SolicitudMapper;
import com.transportes.clients.mapper.UbicacionMapper;
import com.transportes.clients.repository.ContenedorRepository;
import com.transportes.clients.repository.EstadoSolicitudHistorialRepository;
import com.transportes.clients.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ContenedorRepository contenedorRepository;
    private final EstadoSolicitudHistorialRepository historialRepository;
    private final ClienteService clienteService;
    private final SolicitudMapper solicitudMapper;
    private final UbicacionMapper ubicacionMapper;
    private final com.transportes.clients.repository.ClienteRepository clienteRepository;

    @Transactional
    public SolicitudDTO crearSolicitud(CrearSolicitudRequest request, String userEmail) {
        Cliente cliente;
        String emailParaHistorial;

        // Caso 1: Usuario autenticado - usar su email
        if (userEmail != null && !userEmail.isBlank()) {
            log.info("Creando solicitud para usuario autenticado: {}", userEmail);
            cliente = obtenerORegistrarCliente(userEmail);
            emailParaHistorial = userEmail;
        }
        // Caso 2: Usuario NO autenticado - usar datos del request y registrar
        else {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new BusinessException("Debe proporcionar un email o estar autenticado para crear una solicitud");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new BusinessException("Debe proporcionar una contraseña para registrarse");
            }
            if (request.getNombre() == null || request.getNombre().isBlank()) {
                throw new BusinessException("Debe proporcionar un nombre para registrarse");
            }
            if (request.getApellido() == null || request.getApellido().isBlank()) {
                throw new BusinessException("Debe proporcionar un apellido para registrarse");
            }

            log.info("Creando solicitud para nuevo usuario: {}", request.getEmail());

            // Registrar cliente con los datos del request
            cliente = clienteService.registrarClienteConDatos(
                    request.getNombre(),
                    request.getApellido(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getTelefono(),
                    request.getDireccion()
            );
            emailParaHistorial = request.getEmail();
        }

        // Crear contenedor
        Contenedor contenedor = crearContenedor(request.getContenedor(), cliente);

        // Crear ubicaciones
        Ubicacion origen = crearUbicacion(request.getOrigen(), Ubicacion.TipoUbicacion.ORIGEN);
        Ubicacion destino = crearUbicacion(request.getDestino(), Ubicacion.TipoUbicacion.DESTINO);

        // Crear solicitud
        Solicitud solicitud = Solicitud.builder()
                .contenedor(contenedor)
                .cliente(cliente)
                .ubicacionOrigen(origen)
                .ubicacionDestino(destino)
                .estado(Solicitud.EstadoSolicitud.BORRADOR)
                .observaciones(request.getObservaciones())
                .build();

        // Agregar estado inicial al historial
        solicitud.agregarEstadoHistorial(
                Solicitud.EstadoSolicitud.BORRADOR,
                "Solicitud creada",
                emailParaHistorial
        );

        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con número: {}", solicitudGuardada.getNumeroSolicitud());

        return solicitudMapper.toDTO(solicitudGuardada);
    }

    private Contenedor crearContenedor(CrearSolicitudRequest.ContenedorRequest request, Cliente cliente) {
        Contenedor contenedor = Contenedor.builder()
                .pesoKg(request.getPesoKg())
                .volumenM3(request.getVolumenM3())
                .altoM(request.getAltoM())
                .anchoM(request.getAnchoM())
                .largoM(request.getLargoM())
                .descripcion(request.getDescripcion())
                .cliente(cliente)
                .build();

        return contenedorRepository.save(contenedor);
    }

    private Ubicacion crearUbicacion(UbicacionDTO dto, Ubicacion.TipoUbicacion tipo) {
        Ubicacion ubicacion = ubicacionMapper.toEntity(dto);
        ubicacion.setTipo(tipo);
        return ubicacion;
    }

    /**
     * Obtiene un cliente existente o lo registra automáticamente si no existe
     */
    private Cliente obtenerORegistrarCliente(String email) {
        return clienteRepository.findByEmail(email)
                .orElseGet(() -> clienteService.registrarClienteAutomaticamente(email));
    }

    @Transactional(readOnly = true)
    public SolicitudDTO obtenerPorId(Long id, String userEmail) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));

        // Verificar que el usuario tenga acceso a esta solicitud
        verificarAccesoSolicitud(solicitud, userEmail);

        return solicitudMapper.toDTO(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerSolicitudesDelCliente(String userEmail) {
        Cliente cliente = clienteRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con email: " + userEmail));

        List<Solicitud> solicitudes = solicitudRepository.findByClienteIdOrderByFechaCreacionDesc(cliente.getId());

        return solicitudes.stream()
                .map(solicitudMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerTodasLasSolicitudes() {
        return solicitudRepository.findAll().stream()
                .map(solicitudMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerSolicitudesPendientes() {
        return solicitudRepository.findPendientes().stream()
                .map(solicitudMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SeguimientoDTO obtenerSeguimiento(Long solicitudId, String userEmail) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        // Verificar acceso
        verificarAccesoSolicitud(solicitud, userEmail);

        // Obtener historial
        List<EstadoSolicitudHistorial> historial = historialRepository
                .findBySolicitudIdOrderByFechaHoraAsc(solicitudId);

        List<SeguimientoDTO.EstadoHistorialDTO> historialDTO = historial.stream()
                .map(h -> SeguimientoDTO.EstadoHistorialDTO.builder()
                        .estado(h.getEstado().name())
                        .fechaHora(h.getFechaHora())
                        .observacion(h.getObservacion())
                        .build())
                .collect(Collectors.toList());

        return SeguimientoDTO.builder()
                .solicitudId(solicitud.getId())
                .numeroSolicitud(solicitud.getNumeroSolicitud())
                .estadoActual(solicitud.getEstado().name())
                .historial(historialDTO)
                .ubicacionActual(determinarUbicacionActual(solicitud))
                .build();
    }

    private SeguimientoDTO.UbicacionActualDTO determinarUbicacionActual(Solicitud solicitud) {
        switch (solicitud.getEstado()) {
            case BORRADOR:
            case PROGRAMADA:
                return SeguimientoDTO.UbicacionActualDTO.builder()
                        .tipo("ORIGEN")
                        .descripcion("En origen: " + solicitud.getUbicacionOrigen().getDireccion())
                        .build();
            case EN_TRANSITO:
                return SeguimientoDTO.UbicacionActualDTO.builder()
                        .tipo("EN_TRANSITO")
                        .descripcion("En tránsito")
                        .build();
            case ENTREGADA:
                return SeguimientoDTO.UbicacionActualDTO.builder()
                        .tipo("DESTINO")
                        .descripcion("Entregado en: " + solicitud.getUbicacionDestino().getDireccion())
                        .build();
            default:
                return null;
        }
    }

    private void verificarAccesoSolicitud(Solicitud solicitud, String userEmail) {
        // En producción, verificar contra el token JWT
        // Por ahora, verificamos que el email coincida con el del cliente
        if (!solicitud.getCliente().getEmail().equals(userEmail)) {
            throw new BusinessException("No tiene permisos para acceder a esta solicitud");
        }
    }

    @Transactional
    public void actualizarEstado(Long solicitudId, Solicitud.EstadoSolicitud nuevoEstado, String observacion, String usuario) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        solicitud.agregarEstadoHistorial(nuevoEstado, observacion, usuario);
        solicitudRepository.save(solicitud);

        log.info("Estado de solicitud {} actualizado a {}", solicitudId, nuevoEstado);
    }

    @Transactional(readOnly = true)
    public SolicitudDTO obtenerPorIdInternal(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));
        return solicitudMapper.toDTO(solicitud);
    }
}
