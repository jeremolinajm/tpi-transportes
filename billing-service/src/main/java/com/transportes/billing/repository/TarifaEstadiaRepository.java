package com.transportes.billing.repository;

import com.transportes.billing.entity.TarifaEstadia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TarifaEstadiaRepository extends JpaRepository<TarifaEstadia, Long> {
    Optional<TarifaEstadia> findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc();
}
