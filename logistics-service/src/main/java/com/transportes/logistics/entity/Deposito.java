package com.transportes.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "capacidad_maxima_contenedores")
    private Integer capacidadMaximaContenedores = 100;

    @Column(name = "contenedores_actuales")
    private Integer contenedoresActuales = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (capacidadMaximaContenedores == null) {
            capacidadMaximaContenedores = 100;
        }
        if (contenedoresActuales == null) {
            contenedoresActuales = 0;
        }
        if (codigo == null || codigo.isEmpty()) {
            codigo = "DEP-" + java.time.Year.now().getValue() + "-" +
                     String.format("%05d", System.currentTimeMillis() % 100000);
        }
    }
}
