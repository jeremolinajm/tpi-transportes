package com.transportes.logistics.service;

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

    /**
     * Crea un usuario transportista en Keycloak
     */
    public String crearUsuarioTransportista(String email, String password, String nombre, String apellido) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Verificar si el usuario ya existe
            List<UserRepresentation> existingUsers = usersResource.search(email);
            if (!existingUsers.isEmpty()) {
                log.info("El usuario ya existe en Keycloak. Recuperando ID para sincronización.");
                return existingUsers.get(0).getId();
            }

            // Crear usuario
            UserRepresentation user = new UserRepresentation();
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(nombre);
            user.setLastName(apellido);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Crear credencial
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Crear usuario en Keycloak
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromResponse(response);

                // Asignar rol TRANSPORTISTA
                asignarRolTransportista(userId);

                log.info("Usuario transportista creado en Keycloak: {}", email);
                return userId;
            } else {
                String errorMsg = response.readEntity(String.class);
                log.error("Error creating user in Keycloak. Status: {}, Error: {}", response.getStatus(), errorMsg);
                throw new RuntimeException("Error al crear usuario en Keycloak: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("Error al crear usuario transportista en Keycloak", e);
            throw new RuntimeException("Error al crear usuario transportista en Keycloak", e);
        }
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getHeaderString("Location");
        if (location != null) {
            return location.substring(location.lastIndexOf('/') + 1);
        }
        throw new RuntimeException("No se pudo obtener el ID del usuario creado");
    }

    private void asignarRolTransportista(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);

            // Buscar rol TRANSPORTISTA
            var roleRepresentation = realmResource.roles().get("TRANSPORTISTA").toRepresentation();

            // Asignar rol al usuario
            realmResource.users().get(userId).roles().realmLevel()
                    .add(Collections.singletonList(roleRepresentation));

            log.info("Rol TRANSPORTISTA asignado al usuario {}", userId);
        } catch (Exception e) {
            log.error("Error al asignar rol TRANSPORTISTA", e);
            // No lanzamos excepción para no interrumpir el flujo
        }
    }
}
