package com.transportes.clients.mapper;

import com.transportes.clients.dto.UbicacionDTO;
import com.transportes.clients.entity.Ubicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UbicacionMapper {
    
    UbicacionDTO toDTO(Ubicacion ubicacion);
    
    Ubicacion toEntity(UbicacionDTO dto);
}
