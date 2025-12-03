package com.transportes.clients.repository;

import com.transportes.clients.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByNumeroSolicitud(String numeroSolicitud);

    List<Solicitud> findByClienteId(Long clienteId);

    List<Solicitud> findByEstado(Solicitud.EstadoSolicitud estado);

    Optional<Solicitud> findByContenedorId(Long contenedorId);

    @Query("SELECT s FROM Solicitud s WHERE s.cliente.id = :clienteId ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findByClienteIdOrderByFechaCreacionDesc(@Param("clienteId") Long clienteId);

    @Query("SELECT s FROM Solicitud s WHERE s.estado IN :estados ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findByEstadoInOrderByFechaCreacionDesc(@Param("estados") List<Solicitud.EstadoSolicitud> estados);

    @Query("SELECT s FROM Solicitud s WHERE s.estado <> 'ENTREGADA' AND s.estado <> 'CANCELADA' ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findPendientes();
}
