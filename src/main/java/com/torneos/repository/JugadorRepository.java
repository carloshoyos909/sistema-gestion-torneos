package com.torneos.repository;

import com.torneos.model.Jugador;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    boolean existsByEquipoIdAndNombreIgnoreCase(Long equipoId, String nombre);

    @EntityGraph(attributePaths = "equipo")
    List<Jugador> findByEquipoTorneoIdOrderByNombreAsc(Long torneoId);
}
