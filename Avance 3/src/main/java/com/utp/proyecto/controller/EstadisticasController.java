package com.utp.proyecto.controller;

import com.utp.proyecto.service.LugarTuristicoService;
import com.utp.proyecto.service.ServicioTuristicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/estadisticas", "/api/servicios/estadisticas"})
public class EstadisticasController {
    private final LugarTuristicoService lugares;
    private final ServicioTuristicoService servicios;

    public EstadisticasController(LugarTuristicoService lugares, ServicioTuristicoService servicios) {
        this.lugares = lugares;
        this.servicios = servicios;
    }

    @GetMapping
    public Map<String, Integer> estadisticas() {
        return Map.of(
                "lugaresRegistrados", lugares.listarLugares().size(),
                "serviciosRegistrados", servicios.listarServicios().size());
    }
}
