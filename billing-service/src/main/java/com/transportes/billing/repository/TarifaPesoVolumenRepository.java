package com.transportes.billing.repository;

import com.transportes.billing.entity.TarifaPesoVolumen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface TarifaPesoVolumenRepository extends JpaRepository<TarifaPesoVolumen, Long> {
    @Query("SELECT t FROM TarifaPesoVolumen t WHERE t.activa = true " +
           "AND :peso BETWEEN t.pesoMinimoKg AND t.pesoMaximoKg " +
           "AND :volumen BETWEEN t.volumenMinimoM3 AND t.volumenMaximoM3 " +
           "ORDER BY t.fechaVigenciaDesde DESC")
    Optional<TarifaPesoVolumen> findByPesoYVolumen(@Param("peso") BigDecimal peso,
                                                     @Param("volumen") BigDecimal volumen);
}
