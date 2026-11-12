package com.utp.proyecto.service;

import com.utp.proyecto.dto.AuthResponse;
import com.utp.proyecto.dto.LoginRequest;
import com.utp.proyecto.dto.RegistroRequest;
import com.utp.proyecto.model.Rol;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.UsuarioRepository;
import com.utp.proyecto.security.JwtService;
import com.utp.proyecto.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setActivo(true);
        usuario.setRoles(Set.of(Rol.TURISTA));
        usuario = usuarios.save(usuario);

        return crearRespuesta(usuario.getEmail(), usuario.getRoles());
    }

    public AuthResponse iniciarSesion(LoginRequest request) {
        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            normalizarEmail(request.email()),
                            request.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos", e);
        }

        if (!(autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new IllegalStateException("El proveedor de autenticación devolvió un principal incompatible");
        }
        Set<Rol> roles = usuario.getRoles();
        return crearRespuesta(autenticacion.getName(), roles);
    }

    private AuthResponse crearRespuesta(String email, Set<Rol> roles) {
        String token = jwtService.generarToken(email);
        Set<String> nombresRoles = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), email, nombresRoles);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
