package com.transportes.logistics.repository;

import com.transportes.logistics.entity.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    
    Optional<Deposito> findByCodigo(String codigo);
    
    List<Deposito> findByActivoTrue();
    
    boolean existsByCodigo(String codigo);
}
