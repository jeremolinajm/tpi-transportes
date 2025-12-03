package com.transportes.logistics.repository;

import com.transportes.logistics.entity.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    
    List<Tramo> findByRutaIdOrderByNumeroOrdenAsc(Long rutaId);
    
    List<Tramo> findByCamionId(Long camionId);
    
    @Query("SELECT t FROM Tramo t WHERE t.camion.transportista.keycloakUserId = :keycloakUserId " +
           "AND t.estado IN ('ASIGNADO', 'INICIADO') ORDER BY t.fechaHoraInicioEstimada ASC")
    List<Tramo> findTramosAsignadosPorTransportista(@Param("keycloakUserId") String keycloakUserId);
    
    List<Tramo> findByEstado(Tramo.EstadoTramo estado);
}
