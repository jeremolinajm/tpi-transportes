package com.transportes.clients.mapper;

import com.transportes.clients.dto.SolicitudDTO;
import com.transportes.clients.entity.Solicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ClienteMapper.class, ContenedorMapper.class, UbicacionMapper.class})
public interface SolicitudMapper {

    @Mapping(target = "contenedor", source = "contenedor")
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "origen", source = "ubicacionOrigen")
    @Mapping(target = "destino", source = "ubicacionDestino")
    SolicitudDTO toDTO(Solicitud solicitud);
}
