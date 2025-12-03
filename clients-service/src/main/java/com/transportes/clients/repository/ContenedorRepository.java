package com.transportes.clients.repository;

import com.transportes.clients.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    
    Optional<Contenedor> findByCodigo(String codigo);
    
    List<Contenedor> findByClienteId(Long clienteId);
    
    boolean existsByCodigo(String codigo);
}
