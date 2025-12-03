package com.transportes.clients.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearSolicitudRequest {

    @Valid
    @NotNull(message = "Los datos del contenedor son obligatorios")
    private ContenedorRequest contenedor;

    @Valid
    @NotNull(message = "El origen es obligatorio")
    private UbicacionDTO origen;

    @Valid
    @NotNull(message = "El destino es obligatorio")
    private UbicacionDTO destino;

    private String observaciones;

    // Datos del cliente (opcionales si ya está autenticado)
    private String nombre;
    private String apellido;

    @Email(message = "El email debe ser válido")
    private String email;

    private String password;
    private String telefono;
    private String direccion;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContenedorRequest {
        
        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
        private BigDecimal pesoKg;
        
        @NotNull(message = "El volumen es obligatorio")
        @DecimalMin(value = "0.01", message = "El volumen debe ser mayor a 0")
        private BigDecimal volumenM3;
        
        private BigDecimal altoM;
        private BigDecimal anchoM;
        private BigDecimal largoM;
        private String descripcion;
    }
}
