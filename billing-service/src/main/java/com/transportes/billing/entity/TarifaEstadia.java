package com.transportes.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifa_estadia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaEstadia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "costo_por_dia", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoPorDia;

    @Column(name = "costo_por_hora", nullable = false, precision = 8, scale = 2)
    private BigDecimal costoPorHora;

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
