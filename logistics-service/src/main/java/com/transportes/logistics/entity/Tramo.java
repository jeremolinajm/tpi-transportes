package com.transportes.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(name = "numero_orden", nullable = false)
    private Integer numeroOrden;

    @Column(name = "tipo_tramo", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TipoTramo tipoTramo;

    // Origen del tramo
    @Column(name = "origen_tipo", nullable = false, length = 20)
    private String origenTipo;

    @Column(name = "origen_direccion", nullable = false, columnDefinition = "TEXT")
    private String origenDireccion;

    @Column(name = "origen_latitud", nullable = false, precision = 10, scale = 8)
    private BigDecimal origenLatitud;

    @Column(name = "origen_longitud", nullable = false, precision = 11, scale = 8)
    private BigDecimal origenLongitud;

    @Column(name = "origen_deposito_id")
    private Long origenDepositoId;

    // Destino del tramo
    @Column(name = "destino_tipo", nullable = false, length = 20)
    private String destinoTipo;

    @Column(name = "destino_direccion", nullable = false, columnDefinition = "TEXT")
    private String destinoDireccion;

    @Column(name = "destino_latitud", nullable = false, precision = 10, scale = 8)
    private BigDecimal destinoLatitud;

    @Column(name = "destino_longitud", nullable = false, precision = 11, scale = 8)
    private BigDecimal destinoLongitud;

    @Column(name = "destino_deposito_id")
    private Long destinoDepositoId;

    // Datos del tramo
    @Column(name = "distancia_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanciaKm;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoTramo estado = EstadoTramo.ESTIMADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id")
    private Camion camion;

    // Tiempos
    @Column(name = "fecha_hora_inicio_estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @Column(name = "fecha_hora_fin_estimada")
    private LocalDateTime fechaHoraFinEstimada;

    @Column(name = "fecha_hora_inicio_real")
    private LocalDateTime fechaHoraInicioReal;

    @Column(name = "fecha_hora_fin_real")
    private LocalDateTime fechaHoraFinReal;

    // Costos
    @Column(name = "costo_estimado", precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "costo_real", precision = 10, scale = 2)
    private BigDecimal costoReal;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoTramo.ESTIMADO;
        }
    }

    public enum TipoTramo {
        ORIGEN_DEPOSITO,
        DEPOSITO_DEPOSITO,
        DEPOSITO_DESTINO,
        ORIGEN_DESTINO
    }

    public enum EstadoTramo {
        ESTIMADO,
        ASIGNADO,
        INICIADO,
        FINALIZADO
    }
}
