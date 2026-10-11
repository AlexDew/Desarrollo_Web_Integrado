package com.utp.proyecto.controller;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.ServicioTuristicoService;
import com.utp.proyecto.security.UsuarioAutenticado;
import com.utp.proyecto.validation.ValidacionSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServiciosController {
    private final ServicioTuristicoService servicios;

    public ServiciosController(ServicioTuristicoService servicios) {
        this.servicios = servicios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioTuristico registrarServicio(
            @Validated(ValidacionSolicitud.class) @RequestBody ServicioTuristico servicio,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicios.guardarServicio(servicio, usuario.getId());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioTuristico> actualizarServicio(
            @PathVariable Long id,
            @Validated(ValidacionSolicitud.class) @RequestBody ServicioTuristico servicio,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ofNullable(servicios.actualizarServicio(id, servicio, usuario.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicios.eliminarServicio(id, usuario.getId())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<ServicioTuristico> listarServicios() { return servicios.listarServicios(); }
}
