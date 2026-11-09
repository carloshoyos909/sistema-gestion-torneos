package com.torneos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.torneos.model.Torneo;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<Torneo> findByCreadorUsername(String username);
}
