package com.transportes.clients.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ubicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoUbicacion tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "deposito_id")
    private Long depositoId;

    public enum TipoUbicacion {
        ORIGEN,
        DESTINO,
        DEPOSITO
    }
}
