package com.transportes.logistics.repository;

import com.transportes.logistics.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    List<Ruta> findBySolicitudId(Long solicitudId);
    
    Optional<Ruta> findBySolicitudIdAndSeleccionadaTrue(Long solicitudId);
    
    List<Ruta> findBySolicitudIdAndTipo(Long solicitudId, Ruta.TipoRuta tipo);
}
