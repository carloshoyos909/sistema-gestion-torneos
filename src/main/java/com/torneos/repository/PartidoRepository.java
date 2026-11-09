package com.torneos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.torneos.model.Partido;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByTorneoIdOrderByFechaAscHoraAsc(Long torneoId);
    boolean existsByTorneoId(Long torneoId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM Partido p " +
        "JOIN FETCH p.equipoLocal " +
        "JOIN FETCH p.equipoVisitante " +
        "JOIN FETCH p.torneo " +
        "WHERE p.torneo.id = :torneoId " +
        "ORDER BY p.fecha ASC, p.hora ASC")
    List<Partido> findByTorneoIdWithEquipos(@org.springframework.data.repository.query.Param("torneoId") Long torneoId);
    List<Partido> findByTorneoIdAndFaseStartingWithOrderByFechaAsc(Long torneoId, String fasePrefix);
    boolean existsByTorneoIdAndFaseStartingWith(Long torneoId, String fasePrefix);
    boolean existsByTorneoIdAndJugadoFalse(Long torneoId);
    List<Partido> findByTorneoIdAndFaseOrderByFechaAsc(Long torneoId, String fase);
}
