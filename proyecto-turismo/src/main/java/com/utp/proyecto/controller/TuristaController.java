package com.utp.proyecto.controller;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.CatalogoTuristicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/turista")
public class TuristaController {
    private final CatalogoTuristicoService catalogo;
    public TuristaController(CatalogoTuristicoService catalogo) { this.catalogo = catalogo; }

    @GetMapping("/lugares")
    public List<LugarTuristico> lugares(@RequestParam(required = false) String tipo) {
        return tipo == null ? catalogo.listarLugares() : catalogo.buscarLugaresPorTipo(tipo);
    }
    @GetMapping("/lugares/{id}")
    public ResponseEntity<LugarTuristico> lugar(@PathVariable Long id) {
        return ResponseEntity.ofNullable(catalogo.buscarLugar(id));
    }
    @PostMapping("/lugares/{id}/visitas")
    public ResponseEntity<LugarTuristico> registrarVisita(@PathVariable Long id) {
        return ResponseEntity.ofNullable(catalogo.registrarVisita(id));
    }
    @GetMapping("/servicios")
    public List<ServicioTuristico> servicios(@RequestParam(required = false) String tipo) {
        return tipo == null ? catalogo.listarServicios() : catalogo.buscarServiciosPorTipo(tipo);
    }
    @PostMapping("/servicios/{id}/reservas")
    public ResponseEntity<ServicioTuristico> reservar(@PathVariable Long id) {
        return ResponseEntity.ofNullable(catalogo.registrarAtencion(id));
    }
    @PatchMapping("/lugares/{id}/calificacion")
    public ResponseEntity<LugarTuristico> calificar(@PathVariable Long id, @RequestParam Double valor) {
        if (!catalogo.calificarLugar(id, valor)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ofNullable(catalogo.buscarLugar(id));
    }
}
