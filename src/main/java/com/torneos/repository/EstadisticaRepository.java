package com.torneos.repository;

import com.torneos.model.Estadistica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstadisticaRepository extends JpaRepository<Estadistica, Long> {

    List<Estadistica> findByTorneoIdOrderByGolesDescAsistenciasDesc(Long torneoId);

    Optional<Estadistica> findByJugadorIdAndTorneoId(Long jugadorId, Long torneoId);

    @Query("SELECT e FROM Estadistica e JOIN e.jugador j WHERE e.torneo.id = :torneoId " +
           "AND LOWER(j.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "ORDER BY e.goles DESC, e.asistencias DESC")
    List<Estadistica> buscarPorTorneoYNombreJugador(@Param("torneoId") Long torneoId,
                                                     @Param("nombre") String nombre);
}
