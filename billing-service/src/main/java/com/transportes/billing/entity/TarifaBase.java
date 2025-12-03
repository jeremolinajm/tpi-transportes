package com.transportes.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifa_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "costo_fijo_gestion", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoFijoGestion;

    @Column(name = "costo_adicional_por_tramo", precision = 10, scale = 2)
    private BigDecimal costoAdicionalPorTramo;

    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fechaVigenciaHasta;

    @Column(nullable = false)
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
