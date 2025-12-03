package com.transportes.billing.repository;

import com.transportes.billing.entity.CostoTramo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CostoTramoRepository extends JpaRepository<CostoTramo, Long> {
    List<CostoTramo> findByTramoIdOrderByFechaCalculoDesc(Long tramoId);
}
