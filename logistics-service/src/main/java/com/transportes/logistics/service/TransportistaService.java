package com.transportes.logistics.service;

import com.transportes.logistics.dto.TransportistaDTO;
import com.transportes.logistics.entity.Transportista;
import com.transportes.logistics.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportistaService {

    private final TransportistaRepository transportistaRepository;
    private final KeycloakService keycloakService;

    @Transactional
    public TransportistaDTO crear(TransportistaDTO dto) {
        if (transportistaRepository.findByDni(dto.getDni()).isPresent()) {
            throw new RuntimeException("Ya existe un transportista con ese DNI");
        }

        if (dto.getEmail() == null || dto.getPassword() == null) {
            throw new RuntimeException("Email y contraseña son obligatorios");
        }

        // Crear usuario en Keycloak
        String keycloakId = keycloakService.crearUsuarioTransportista(
                dto.getEmail(),
                dto.getPassword(),
                dto.getNombre(),
                dto.getApellido()
        );

        Transportista entity = Transportista.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .licenciaConducir(dto.getLicenciaConducir())
                .keycloakUserId(keycloakId)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        Transportista guardado = transportistaRepository.save(entity);
        log.info("Transportista creado exitosamente con ID: {} y Keycloak ID: {}", guardado.getId(), keycloakId);

        return convertirADTO(guardado);
    }

    private TransportistaDTO convertirADTO(Transportista entity) {
        return TransportistaDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .dni(entity.getDni())
                .telefono(entity.getTelefono())
                .email(entity.getEmail())
                .licenciaConducir(entity.getLicenciaConducir())
                .keycloakUserId(entity.getKeycloakUserId())
                .activo(entity.getActivo())
                .build();
    }
}
