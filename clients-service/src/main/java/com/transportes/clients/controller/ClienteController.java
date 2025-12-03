package com.transportes.clients.controller;

import com.transportes.clients.dto.ClienteDTO;
import com.transportes.clients.dto.RegistroClienteRequest;
import com.transportes.clients.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clientes", description = "API para gestión de clientes")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Registrar nuevo cliente", description = "Registra un nuevo cliente en el sistema y en Keycloak")
    @PostMapping("/registro")
    public ResponseEntity<ClienteDTO> registrarCliente(@Valid @RequestBody RegistroClienteRequest request) {
        ClienteDTO cliente = clienteService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    @Operation(summary = "Obtener cliente por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerCliente(@PathVariable Long id) {
        ClienteDTO cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Obtener cliente por email")
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteDTO> obtenerClientePorEmail(@PathVariable String email) {
        ClienteDTO cliente = clienteService.obtenerPorEmail(email);
        return ResponseEntity.ok(cliente);
    }
}
