package com.transportes.logistics.repository;

import com.transportes.logistics.entity.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
    
    Optional<Transportista> findByDni(String dni);
    
    Optional<Transportista> findByKeycloakUserId(String keycloakUserId);
}
