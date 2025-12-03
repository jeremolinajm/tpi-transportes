package com.transportes.clients.service;

import com.transportes.clients.dto.RegistroClienteRequest;
import com.transportes.clients.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        try {
            this.keycloak = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm("master")
                    .clientId(adminClientId)
                    .username(adminUsername)
                    .password(adminPassword)
                    .build();
            log.info("Keycloak admin client initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing Keycloak admin client", e);
        }
    }

    public String crearUsuarioCliente(RegistroClienteRequest request) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Verificar si el usuario ya existe
            List<UserRepresentation> existingUsers = usersResource.search(request.getEmail());
            if (!existingUsers.isEmpty()) {
                // CORRECCIÓN: Si existe en Keycloak, devolvemos su ID para permitir
                // que el ClienteService lo guarde en la BD local.
                log.info("El usuario ya existe en Keycloak. Recuperando ID para sincronización.");
                return existingUsers.get(0).getId();
            }

            // Crear usuario
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getEmail());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getNombre());
            user.setLastName(request.getApellido());
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Crear credencial
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Crear usuario en Keycloak
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromResponse(response);
                
                // Asignar rol CLIENTE
                asignarRolCliente(userId);
                
                log.info("Usuario cliente creado en Keycloak: {}", request.getEmail());
                return userId;
            } else {
                String errorMsg = response.readEntity(String.class);
                log.error("Error creating user in Keycloak. Status: {}, Error: {}", response.getStatus(), errorMsg);
                throw new BusinessException("Error al crear usuario en Keycloak: " + errorMsg);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear usuario en Keycloak", e);
            throw new BusinessException("Error al crear usuario en Keycloak", e);
        }
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getHeaderString("Location");
        if (location != null) {
            return location.substring(location.lastIndexOf('/') + 1);
        }
        throw new BusinessException("No se pudo obtener el ID del usuario creado");
    }

    private void asignarRolCliente(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            
            // Buscar rol CLIENTE
            var roleRepresentation = realmResource.roles().get("CLIENTE").toRepresentation();
            
            // Asignar rol al usuario
            realmResource.users().get(userId).roles().realmLevel()
                    .add(Collections.singletonList(roleRepresentation));
            
            log.info("Rol CLIENTE asignado al usuario {}", userId);
        } catch (Exception e) {
            log.error("Error al asignar rol CLIENTE", e);
            // No lanzamos excepción para no interrumpir el flujo
        }
    }

    public void eliminarUsuario(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.users().get(keycloakUserId).remove();
            log.info("Usuario eliminado de Keycloak: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Error al eliminar usuario de Keycloak", e);
        }
    }

    /**
     * Obtiene información de un usuario desde Keycloak por su email
     */
    public UserRepresentation obtenerUsuarioPorEmail(String email) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(email, true);

            if (users.isEmpty()) {
                log.warn("Usuario no encontrado en Keycloak con email: {}", email);
                return null;
            }

            return users.get(0);
        } catch (Exception e) {
            log.error("Error al obtener usuario de Keycloak por email", e);
            return null;
        }
    }
}
