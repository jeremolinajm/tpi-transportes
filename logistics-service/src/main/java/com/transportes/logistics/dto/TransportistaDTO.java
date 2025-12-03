package com.transportes.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportistaDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    private String telefono;
    private String email;

    @NotBlank(message = "La licencia es obligatoria")
    private String licenciaConducir;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String keycloakUserId;
    private Boolean activo;
}