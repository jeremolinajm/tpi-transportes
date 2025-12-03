package com.transportes.clients.mapper;

import com.transportes.clients.dto.ContenedorDTO;
import com.transportes.clients.entity.Contenedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContenedorMapper {
    
    @Mapping(target = "clienteId", source = "cliente.id")
    ContenedorDTO toDTO(Contenedor contenedor);
    
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Contenedor toEntity(ContenedorDTO dto);
}
