package com.transportes.clients.service;

import com.transportes.clients.dto.ClienteDTO;
import com.transportes.clients.dto.RegistroClienteRequest;
import com.transportes.clients.entity.Cliente;
import com.transportes.clients.exception.BusinessException;
import com.transportes.clients.exception.ResourceNotFoundException;
import com.transportes.clients.mapper.ClienteMapper;
import com.transportes.clients.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final KeycloakService keycloakService;

    @Transactional
    public ClienteDTO registrarCliente(RegistroClienteRequest request) {
        log.info("Procesando registro de cliente: {}", request.getEmail());

        // 1. Verificar si ya existe en la BD local
        if (clienteRepository.existsByEmail(request.getEmail())) {
            // Si ya existe en BD, simplemente lo devolvemos (Idempotencia)
            return obtenerPorEmail(request.getEmail());
        }

        // 2. Crear o Recuperar usuario de Keycloak
        String keycloakUserId = keycloakService.crearUsuarioCliente(request);

        // 3. Crear cliente en base de datos local
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setKeycloakUserId(keycloakUserId);

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente sincronizado/registrado exitosamente con ID: {}", clienteGuardado.getId());

        return clienteMapper.toDTO(clienteGuardado);
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return clienteMapper.toDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerPorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con email: " + email));
        return clienteMapper.toDTO(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerClienteEntity(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Cliente obtenerClientePorKeycloakUserId(String keycloakUserId) {
        return clienteRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario"));
    }

    /**
     * Registra automáticamente un cliente desde Keycloak si no existe en la BD local
     */
    @Transactional
    public Cliente registrarClienteAutomaticamente(String email) {
        log.info("Registrando automáticamente cliente desde Keycloak: {}", email);

        // Verificar si ya existe
        if (clienteRepository.existsByEmail(email)) {
            return clienteRepository.findByEmail(email).orElseThrow();
        }

        // Obtener datos del usuario desde Keycloak
        var keycloakUser = keycloakService.obtenerUsuarioPorEmail(email);
        if (keycloakUser == null) {
            throw new BusinessException("No se pudo obtener información del usuario desde Keycloak");
        }

        // Crear cliente con datos disponibles de Keycloak
        Cliente cliente = Cliente.builder()
                .nombre(keycloakUser.getFirstName() != null ? keycloakUser.getFirstName() : "Sin nombre")
                .apellido(keycloakUser.getLastName() != null ? keycloakUser.getLastName() : "Sin apellido")
                .email(email)
                .keycloakUserId(keycloakUser.getId())
                .activo(true)
                .build();

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente registrado automáticamente con ID: {}", clienteGuardado.getId());

        return clienteGuardado;
    }

    /**
     * Registra un cliente con datos proporcionados (para solicitudes sin autenticación)
     */
    @Transactional
    public Cliente registrarClienteConDatos(String nombre, String apellido, String email,
                                            String password, String telefono, String direccion) {
        log.info("Registrando nuevo cliente con datos proporcionados: {}", email);

        // Verificar si ya existe
        if (clienteRepository.existsByEmail(email)) {
            log.info("Cliente ya existe con email: {}", email);
            return clienteRepository.findByEmail(email).orElseThrow();
        }

        // Crear request para registrar en Keycloak
        RegistroClienteRequest request = RegistroClienteRequest.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .password(password)
                .telefono(telefono)
                .direccion(direccion)
                .build();

        // Crear usuario en Keycloak
        String keycloakUserId = keycloakService.crearUsuarioCliente(request);

        // Crear cliente en BD local
        Cliente cliente = Cliente.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .telefono(telefono)
                .direccion(direccion)
                .keycloakUserId(keycloakUserId)
                .activo(true)
                .build();

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente registrado con ID: {}", clienteGuardado.getId());

        return clienteGuardado;
    }
}
