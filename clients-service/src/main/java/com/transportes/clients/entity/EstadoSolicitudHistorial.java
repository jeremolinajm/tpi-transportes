package com.transportes.clients.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "estado_solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoSolicitudHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Solicitud.EstadoSolicitud estado;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(length = 100)
    private String usuario;

    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}
