package com.transportes.logistics.repository;

import com.transportes.logistics.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    
    Optional<Camion> findByDominio(String dominio);
    
    List<Camion> findByEstado(Camion.EstadoCamion estado);
    
    @Query("SELECT c FROM Camion c WHERE c.estado = 'DISPONIBLE' AND c.activo = true " +
           "AND c.capacidadPesoKg >= :pesoRequerido AND c.capacidadVolumenM3 >= :volumenRequerido")
    List<Camion> findCamionesDisponiblesParaContenedor(
            @Param("pesoRequerido") BigDecimal pesoRequerido,
            @Param("volumenRequerido") BigDecimal volumenRequerido);
    
    List<Camion> findByActivoTrue();
}
