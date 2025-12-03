package com.transportes.clients.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_solicitud", nullable = false, unique = true, length = 50)
    private String numeroSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ubicacion_origen_id", nullable = false)
    private Ubicacion ubicacionOrigen;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ubicacion_destino_id", nullable = false)
    private Ubicacion ubicacionDestino;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "tiempo_estimado_horas")
    private Integer tiempoEstimadoHoras;

    @Column(name = "costo_final", precision = 12, scale = 2)
    private BigDecimal costoFinal;

    @Column(name = "tiempo_real_horas")
    private Integer tiempoRealHoras;

    @Column(name = "ruta_id")
    private Long rutaId;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EstadoSolicitudHistorial> historialEstados = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaUltimaActualizacion = LocalDateTime.now();
        if (numeroSolicitud == null || numeroSolicitud.isEmpty()) {
            numeroSolicitud = generarNumeroSolicitud();
        }
        if (estado == null) {
            estado = EstadoSolicitud.BORRADOR;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaUltimaActualizacion = LocalDateTime.now();
    }

    private String generarNumeroSolicitud() {
        return "SOL-" + java.time.Year.now().getValue() + "-" + 
               String.format("%05d", System.currentTimeMillis() % 100000);
    }

    public void agregarEstadoHistorial(EstadoSolicitud nuevoEstado, String observacion, String usuario) {
        EstadoSolicitudHistorial historial = EstadoSolicitudHistorial.builder()
                .solicitud(this)
                .estado(nuevoEstado)
                .observacion(observacion)
                .usuario(usuario)
                .build();
        historialEstados.add(historial);
        this.estado = nuevoEstado;
    }

    public enum EstadoSolicitud {
        BORRADOR,
        PROGRAMADA,
        EN_TRANSITO,
        ENTREGADA,
        CANCELADA
    }
}
