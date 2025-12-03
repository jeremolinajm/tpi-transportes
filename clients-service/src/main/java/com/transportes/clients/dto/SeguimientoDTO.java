package com.transportes.clients.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoDTO {
    private Long solicitudId;
    private String numeroSolicitud;
    private String estadoActual;
    private List<EstadoHistorialDTO> historial;
    private UbicacionActualDTO ubicacionActual;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstadoHistorialDTO {
        private String estado;
        private LocalDateTime fechaHora;
        private String observacion;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UbicacionActualDTO {
        private String descripcion;
        private String tipo;
    }
}
