package com.transportes.clients.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contenedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "volumen_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenM3;

    @Column(name = "alto_m", precision = 5, scale = 2)
    private BigDecimal altoM;

    @Column(name = "ancho_m", precision = 5, scale = 2)
    private BigDecimal anchoM;

    @Column(name = "largo_m", precision = 5, scale = 2)
    private BigDecimal largoM;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (codigo == null || codigo.isEmpty()) {
            codigo = generarCodigo();
        }
    }

    private String generarCodigo() {
        return "CONT-" + System.currentTimeMillis();
    }
}
