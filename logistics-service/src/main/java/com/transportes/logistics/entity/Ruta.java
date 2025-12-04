package com.transportes.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ruta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TipoRuta tipo = TipoRuta.PROPUESTA;

    @Column(name = "indice")
    private Integer indice; // Índice de la ruta seleccionada (0, 1, 2)

    @Column(name = "descripcion", length = 255)
    private String descripcion; // Descripción de la ruta

    @Column(name = "cantidad_tramos", nullable = false)
    private Integer cantidadTramos;

    @Column(name = "cantidad_depositos")
    private Integer cantidadDepositos = 0;

    @Column(name = "distancia_total_km", precision = 10, scale = 2)
    private BigDecimal distanciaTotalKm;

    @Column(name = "tiempo_estimado_total_horas")
    private Integer tiempoEstimadoTotalHoras;

    @Column(name = "costo_estimado_total", precision = 12, scale = 2)
    private BigDecimal costoEstimadoTotal;

    @Column(name = "costo_real_total", precision = 12, scale = 2)
    private BigDecimal costoRealTotal;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Boolean seleccionada = false;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroOrden ASC")
    @Builder.Default
    private List<Tramo> tramos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (tipo == null) {
            tipo = TipoRuta.PROPUESTA;
        }
        if (seleccionada == null) {
            seleccionada = false;
        }
        if (cantidadDepositos == null) {
            cantidadDepositos = 0;
        }
    }

    public enum TipoRuta {
        PROPUESTA,
        ASIGNADA
    }
}
