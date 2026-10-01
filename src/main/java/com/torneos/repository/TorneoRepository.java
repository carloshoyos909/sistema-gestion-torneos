package com.torneos.repository;

import com.torneos.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
