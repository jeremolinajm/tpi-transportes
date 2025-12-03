package com.transportes.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "costo_tramo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoTramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tramo_id", nullable = false)
    private Long tramoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costo_solicitud_id", nullable = false)
    private CostoSolicitud costoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCostoTramo tipo;

    // Costos del tramo
    @Column(name = "distancia_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanciaKm;

    @Column(name = "costo_por_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal costoPorKm;

    @Column(name = "costo_combustible", precision = 10, scale = 2)
    private BigDecimal costoCombustible;

    @Column(name = "costo_estadia", precision = 10, scale = 2)
    private BigDecimal costoEstadia;

    @Column(name = "horas_estadia", precision = 8, scale = 2)
    private BigDecimal horasEstadia;

    // Total del tramo
    @Column(name = "costo_total_tramo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoTotalTramo;

    @CreationTimestamp
    @Column(name = "fecha_calculo", updatable = false)
    private LocalDateTime fechaCalculo;

    public enum TipoCostoTramo {
        ESTIMADO, REAL
    }
}
