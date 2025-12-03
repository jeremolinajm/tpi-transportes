package com.transportes.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transportista")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(name = "licencia_conducir", nullable = false, length = 50)
    private String licenciaConducir;

    @Column(name = "keycloak_user_id", unique = true, length = 100)
    private String keycloakUserId;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }
}
