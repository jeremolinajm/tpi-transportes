package com.transportes.clients.dto;

import com.transportes.clients.entity.Solicitud;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudDTO {
    private Long id;
    private String numeroSolicitud;
    private ContenedorDTO contenedor;
    private ClienteDTO cliente;
    private UbicacionDTO origen;
    private UbicacionDTO destino;
    private Solicitud.EstadoSolicitud estado;
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoHoras;
    private BigDecimal costoFinal;
    private Integer tiempoRealHoras;
    private Long rutaId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;
    private String observaciones;
}
