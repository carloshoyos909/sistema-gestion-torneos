package com.torneos.repository;

import com.torneos.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    boolean existsByTorneoIdAndNombreIgnoreCase(Long torneoId, String nombre);
    List<Equipo> findByTorneoIdOrderByNombreAsc(Long torneoId);
    Optional<Equipo> findByIdAndTorneoId(Long id, Long torneoId);
}
