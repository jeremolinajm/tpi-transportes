package com.transportes.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifa_peso_volumen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaPesoVolumen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "peso_minimo_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoMinimoKg;

    @Column(name = "peso_maximo_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoMaximoKg;

    @Column(name = "volumen_minimo_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenMinimoM3;

    @Column(name = "volumen_maximo_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenMaximoM3;

    @Column(name = "multiplicador_costo", precision = 5, scale = 2)
    private BigDecimal multiplicadorCosto;

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
