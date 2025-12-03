package com.transportes.billing.repository;

import com.transportes.billing.entity.CostoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CostoSolicitudRepository extends JpaRepository<CostoSolicitud, Long> {
    List<CostoSolicitud> findBySolicitudIdOrderByFechaCalculoDesc(Long solicitudId);
    Optional<CostoSolicitud> findBySolicitudIdAndTipo(Long solicitudId, CostoSolicitud.TipoCosto tipo);
}
