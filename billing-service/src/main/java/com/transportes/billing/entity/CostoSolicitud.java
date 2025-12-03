package com.transportes.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "costo_solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCosto tipo;

    // Costos desglosados
    @Column(name = "costo_gestion", precision = 10, scale = 2)
    private BigDecimal costoGestion;

    @Column(name = "costo_transporte", precision = 10, scale = 2)
    private BigDecimal costoTransporte;

    @Column(name = "costo_combustible", precision = 10, scale = 2)
    private BigDecimal costoCombustible;

    @Column(name = "costo_estadia", precision = 10, scale = 2)
    private BigDecimal costoEstadia;

    @Column(name = "costo_adicionales", precision = 10, scale = 2)
    private BigDecimal costoAdicionales;

    // Total
    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    // Referencias a tarifas usadas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_base_id")
    private TarifaBase tarifaBase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_combustible_id")
    private TarifaCombustible tarifaCombustible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_estadia_id")
    private TarifaEstadia tarifaEstadia;

    @CreationTimestamp
    @Column(name = "fecha_calculo", updatable = false)
    private LocalDateTime fechaCalculo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "costoSolicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CostoTramo> costosTramos = new ArrayList<>();

    public enum TipoCosto {
        ESTIMADO, FINAL
    }

    // Helper methods
    public void agregarCostoTramo(CostoTramo costoTramo) {
        costosTramos.add(costoTramo);
        costoTramo.setCostoSolicitud(this);
    }
}
