package com.utp.proyecto.controller;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.service.LugarTuristicoService;
import com.utp.proyecto.security.UsuarioAutenticado;
import com.utp.proyecto.validation.ValidacionSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LugaresController {
    private final LugarTuristicoService lugares;

    public LugaresController(LugarTuristicoService lugares) {
        this.lugares = lugares;
    }

    @PostMapping({"/api/lugares", "/api/servicios/lugares"})
    @ResponseStatus(HttpStatus.CREATED)
    public LugarTuristico registrarLugar(
            @Validated(ValidacionSolicitud.class) @RequestBody LugarTuristico lugar,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return lugares.guardarLugar(lugar, usuario.getId());
    }

    @PutMapping({"/api/lugares/{id}", "/api/servicios/lugares/{id}"})
    public ResponseEntity<LugarTuristico> actualizarLugar(
            @PathVariable Long id,
            @Validated(ValidacionSolicitud.class) @RequestBody LugarTuristico lugar,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ofNullable(lugares.actualizarLugar(id, lugar, usuario.getId()));
    }

    @DeleteMapping({"/api/lugares/{id}", "/api/servicios/lugares/{id}"})
    public ResponseEntity<Void> eliminarLugar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return lugares.eliminarLugar(id, usuario.getId())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping({"/api/lugares", "/api/turista/lugares"})
    public List<LugarTuristico> listarLugares(@RequestParam(required = false) String tipo) {
        return tipo == null ? lugares.listarLugares() : lugares.buscarLugaresPorTipo(tipo);
    }

    @GetMapping({"/api/lugares/{id}", "/api/turista/lugares/{id}"})
    public ResponseEntity<LugarTuristico> lugar(@PathVariable Long id) {
        return ResponseEntity.ofNullable(lugares.buscarLugar(id));
    }

    @PostMapping({"/api/lugares/{id}/visitas", "/api/turista/lugares/{id}/visitas"})
    public ResponseEntity<LugarTuristico> registrarVisita(@PathVariable Long id) {
        return ResponseEntity.ofNullable(lugares.registrarVisita(id));
    }

    @PatchMapping({"/api/lugares/{id}/calificacion", "/api/turista/lugares/{id}/calificacion"})
    public ResponseEntity<LugarTuristico> calificar(
            @PathVariable Long id,
            @RequestParam Double valor) {
        if (!lugares.calificarLugar(id, valor)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ofNullable(lugares.buscarLugar(id));
    }
}
