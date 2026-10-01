package com.torneos.controller;

import com.torneos.dto.EquipoRequest;
import com.torneos.dto.EquipoResponse;
import com.torneos.service.EquipoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos/{torneoId}/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse registrar(@PathVariable Long torneoId,
                                    @Valid @RequestBody EquipoRequest request) {
        return equipoService.registrarEquipo(
                torneoId,
                request.nombre(),
                request.jugadores()
        );
    }

    @GetMapping
    public List<EquipoResponse> listar(@PathVariable Long torneoId) {
        return equipoService.getEquipos(torneoId);
    }
}
