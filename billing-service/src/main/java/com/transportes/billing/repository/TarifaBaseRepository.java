package com.transportes.billing.repository;

import com.transportes.billing.entity.TarifaBase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TarifaBaseRepository extends JpaRepository<TarifaBase, Long> {
    Optional<TarifaBase> findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc();
}
