package com.torneos.controller;

import com.torneos.dto.PartidoRequest;
import com.torneos.dto.PartidoResponse;
import com.torneos.service.PartidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos/{torneoId}/partidos")
public class PartidoController {

    private final PartidoService partidoService;

    public PartidoController(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidoResponse programar(@PathVariable Long torneoId,
                                     @Valid @RequestBody PartidoRequest request) {
        return partidoService.programarPartido(
                torneoId,
                request.equipoLocalId(),
                request.equipoVisitanteId(),
                request.fecha(),
                request.hora(),
                request.cancha()
        );
    }

    @GetMapping
    public List<PartidoResponse> listar(@PathVariable Long torneoId) {
        return partidoService.getPartidos(torneoId);
    }
}
