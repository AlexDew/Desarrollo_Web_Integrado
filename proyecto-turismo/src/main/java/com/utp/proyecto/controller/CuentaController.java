package com.utp.proyecto.controller;

import com.utp.proyecto.dto.CuentaRolesResponse;
import com.utp.proyecto.dto.SolicitudRolRequest;
import com.utp.proyecto.security.UsuarioAutenticado;
import com.utp.proyecto.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {
    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping("/roles")
    public CuentaRolesResponse agregarRol(
            @Valid @RequestBody SolicitudRolRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return new CuentaRolesResponse(cuentaService.agregarRol(usuario.getId(), request.rol()));
    }
}
