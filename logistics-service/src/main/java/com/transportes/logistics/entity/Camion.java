package com.transportes.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "camion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String dominio;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    private Integer anio;

    @Column(name = "capacidad_peso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPesoKg;

    @Column(name = "capacidad_volumen_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumenM3;

    @Column(name = "consumo_combustible_km_litro", nullable = false, precision = 5, scale = 2)
    private BigDecimal consumoCombustibleKmLitro;

    @Column(name = "costo_base_por_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal costoBasePorKm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Transportista transportista;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoCamion estado = EstadoCamion.DISPONIBLE;

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
        if (estado == null) {
            estado = EstadoCamion.DISPONIBLE;
        }
    }

    public enum EstadoCamion {
        DISPONIBLE,
        OCUPADO,
        MANTENIMIENTO,
        INACTIVO
    }
}
