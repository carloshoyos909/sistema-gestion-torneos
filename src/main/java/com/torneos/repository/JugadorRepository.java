package com.torneos.repository;

import com.torneos.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    boolean existsByEquipoIdAndNombreIgnoreCase(Long equipoId, String nombre);
}
