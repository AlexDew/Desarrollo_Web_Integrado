package com.utp.proyecto.security;

import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta con ese correo"));
        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                Boolean.TRUE.equals(usuario.getActivo()),
                usuario.getRoles());
    }
}
