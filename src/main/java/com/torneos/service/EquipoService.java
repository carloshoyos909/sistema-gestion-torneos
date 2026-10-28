package com.torneos.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torneos.dto.EquipoResponse;
import com.torneos.model.Equipo;
import com.torneos.model.Jugador;
import com.torneos.model.Rol;
import com.torneos.model.Torneo;
import com.torneos.model.Usuario;
import com.torneos.repository.EquipoRepository;
import com.torneos.repository.TorneoRepository;
import com.torneos.repository.UsuarioRepository;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public EquipoService(EquipoRepository equipoRepository,
                         TorneoRepository torneoRepository,
                         UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EquipoResponse registrarEquipo(Long torneoId, String nombre, List<String> nombresJugadores) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("El torneo no existe."));

        String nombreNormalizado = nombre.trim();

        if (equipoRepository.existsByTorneoIdAndNombreIgnoreCase(torneoId, nombreNormalizado)) {
            throw new IllegalArgumentException("El nombre del equipo ya se encuentra registrado en este torneo.");
        }

        Equipo equipo = new Equipo(nombreNormalizado, torneo);

        if (nombresJugadores != null) {
            nombresJugadores.stream()
                    .filter(n -> n != null && !n.isBlank())
                    .map(String::trim)
                    .distinct()
                    .forEach(nombreJugador -> {
                        equipo.agregarJugador(nombreJugador);
                        crearUsuarioJugadorSiNoExiste(nombreJugador);
                    });
        }

        return toResponse(equipoRepository.save(equipo));
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> getEquipos(Long torneoId) {
        validarTorneo(torneoId);
        return equipoRepository.findByTorneoIdOrderByNombreAsc(torneoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Equipo buscarEntidadPorId(Long torneoId, Long equipoId) {
        return equipoRepository.findByIdAndTorneoId(equipoId, torneoId)
                .orElseThrow(() -> new IllegalArgumentException("El equipo no existe en el torneo seleccionado."));
    }

    @Transactional(readOnly = true)
    public List<Equipo> getEntidades(Long torneoId) {
        validarTorneo(torneoId);
        return equipoRepository.findByTorneoIdOrderByNombreAsc(torneoId);
    }

    // Crea automáticamente un usuario con rol JUGADOR para el nombre dado.
    // username = nombre sin espacios ni tildes, en minúsculas (ej. "Ángel Pérez" -> "angelperez")
    // password = "12345" encriptada con BCrypt
    private void crearUsuarioJugadorSiNoExiste(String nombreJugador) {
        String username = normalizarUsername(nombreJugador);
        if (!username.isBlank() && !usuarioRepository.existsByUsername(username)) {
            Usuario usuario = new Usuario(username, passwordEncoder.encode("12345"), Rol.JUGADOR);
            usuarioRepository.save(usuario);
        }
    }

    private String normalizarUsername(String nombreJugador) {
        String sinAcentos = Normalizer.normalize(nombreJugador, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        return sinAcentos.length() <= 60 ? sinAcentos : sinAcentos.substring(0, 60);
    }

    private void validarTorneo(Long torneoId) {
        if (!torneoRepository.existsById(torneoId)) {
            throw new IllegalArgumentException("El torneo no existe.");
        }
    }

    private EquipoResponse toResponse(Equipo equipo) {
        return new EquipoResponse(
                equipo.getId(),
                equipo.getNombre(),
                equipo.getJugadores().stream()
                        .map(Jugador::getNombre)
                        .toList()
        );
    }
}
