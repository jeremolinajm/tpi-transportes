package com.transportes.billing.repository;

import com.transportes.billing.entity.TarifaCombustible;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TarifaCombustibleRepository extends JpaRepository<TarifaCombustible, Long> {
    Optional<TarifaCombustible> findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc();
}
