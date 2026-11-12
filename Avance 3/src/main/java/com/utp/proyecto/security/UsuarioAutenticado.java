package com.utp.proyecto.security;

import com.utp.proyecto.model.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UsuarioAutenticado implements UserDetails {
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final boolean activo;
    private final Set<Rol> roles;

    public UsuarioAutenticado(Long id, String email, String passwordHash, boolean activo, Set<Rol> roles) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.activo = activo;
        this.roles = Set.copyOf(roles);
    }

    public Long getId() {
        return id;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
