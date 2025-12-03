package com.transportes.clients.mapper;

import com.transportes.clients.dto.ClienteDTO;
import com.transportes.clients.dto.RegistroClienteRequest;
import com.transportes.clients.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    
    ClienteDTO toDTO(Cliente cliente);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakUserId", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", constant = "true")
    Cliente toEntity(RegistroClienteRequest request);
}
