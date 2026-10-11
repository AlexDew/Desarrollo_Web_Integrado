package com.utp.proyecto.controller;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.ServicioTuristicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/turista")
public class TuristaController {
    private final ServicioTuristicoService servicios;

    public TuristaController(ServicioTuristicoService servicios) {
        this.servicios = servicios;
    }

    @GetMapping("/servicios")
    public List<ServicioTuristico> servicios(@RequestParam(required = false) String tipo) {
        return tipo == null ? servicios.listarServicios() : servicios.buscarServiciosPorTipo(tipo);
    }

    @PostMapping("/servicios/{id}/reservas")
    public ResponseEntity<ServicioTuristico> reservar(@PathVariable Long id) {
        return ResponseEntity.ofNullable(servicios.registrarAtencion(id));
    }
}
