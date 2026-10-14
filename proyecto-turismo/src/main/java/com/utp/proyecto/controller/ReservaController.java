package com.utp.proyecto.controller;

import com.utp.proyecto.dto.CambiarEstadoReservaRequest;
import com.utp.proyecto.dto.CrearReservaRequest;
import com.utp.proyecto.dto.ReservaResponse;
import com.utp.proyecto.security.UsuarioAutenticado;
import com.utp.proyecto.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReservaController {
    private final ReservaService reservas;

    public ReservaController(ReservaService reservas) {
        this.reservas = reservas;
    }

    @PostMapping("/api/turista/servicios/{servicioId}/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(
            @PathVariable Long servicioId,
            @Valid @RequestBody CrearReservaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado turista) {
        return reservas.crear(servicioId, turista.getId(), request);
    }

    @GetMapping("/api/turista/reservas")
    public List<ReservaResponse> listarDelTurista(
            @AuthenticationPrincipal UsuarioAutenticado turista) {
        return reservas.listarDelTurista(turista.getId());
    }

    @DeleteMapping("/api/turista/reservas/{reservaId}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long reservaId,
            @AuthenticationPrincipal UsuarioAutenticado turista) {
        reservas.cancelar(reservaId, turista.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/servicios/reservas")
    public List<ReservaResponse> listarDeServiciosDelPropietario(
            @AuthenticationPrincipal UsuarioAutenticado propietario) {
        return reservas.listarDeServiciosDelPropietario(propietario.getId());
    }

    @PatchMapping("/api/servicios/reservas/{reservaId}/estado")
    public ReservaResponse cambiarEstado(
            @PathVariable Long reservaId,
            @Valid @RequestBody CambiarEstadoReservaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado propietario) {
        return reservas.cambiarEstado(reservaId, propietario.getId(), request.estado());
    }
}
