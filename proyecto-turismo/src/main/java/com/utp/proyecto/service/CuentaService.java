package com.utp.proyecto.service;

import com.utp.proyecto.model.Rol;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CuentaService {
    private final UsuarioRepository usuarios;

    public CuentaService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional
    public List<String> agregarRol(Long usuarioId, Rol rol) {
        if (rol != Rol.GUIA && rol != Rol.PROPIETARIO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden agregar los roles GUIA o PROPIETARIO");
        }

        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la cuenta"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está deshabilitada");
        }

        usuario.getRoles().add(rol);
        usuarios.save(usuario);

        return usuario.getRoles().stream()
                .map(Enum::name)
                .sorted()
                .toList();
    }
}
