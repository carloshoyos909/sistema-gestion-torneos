package com.torneos.controller;

import com.torneos.dto.TorneoRequest;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TorneoResponse crear(@Valid @RequestBody TorneoRequest request) {
        return torneoService.crearTorneo(
                request.nombre(),
                request.deporte(),
                request.categoria()
        );
    }

    @GetMapping
    public List<TorneoResponse> listar() {
        return torneoService.getTorneos();
    }
}
