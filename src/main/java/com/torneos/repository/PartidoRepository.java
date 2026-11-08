package com.torneos.repository;

import com.torneos.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByTorneoIdOrderByFechaAscHoraAsc(Long torneoId);
    List<Partido> findByTorneoIdAndFaseStartingWithOrderByFechaAsc(Long torneoId, String fasePrefix);
    boolean existsByTorneoIdAndFaseStartingWith(Long torneoId, String fasePrefix);
    boolean existsByTorneoIdAndJugadoFalse(Long torneoId);
    List<Partido> findByTorneoIdAndFaseOrderByFechaAsc(Long torneoId, String fase);
}
