package com.transportes.clients.repository;

import com.transportes.clients.entity.EstadoSolicitudHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EstadoSolicitudHistorialRepository extends JpaRepository<EstadoSolicitudHistorial, Long> {
    
    List<EstadoSolicitudHistorial> findBySolicitudIdOrderByFechaHoraAsc(Long solicitudId);
}
