package com.utp.proyecto.config;

import com.utp.proyecto.security.JwtAuthenticationFilter;
import com.utp.proyecto.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(daoAuthenticationProvider)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, error) ->
                                response.sendError(401, "Autenticación requerida"))
                        .accessDeniedHandler((request, response, error) ->
                                response.sendError(403, "No tienes permiso para esta operación")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/registro", "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cuenta/roles").authenticated()

                        .requestMatchers(HttpMethod.GET,
                                "/api/lugares", "/api/lugares/**",
                                "/api/turista/lugares", "/api/turista/lugares/**",
                                "/api/turista/servicios", "/api/turista/servicios/**").permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/turista/lugares/*/visitas",
                                "/api/lugares/*/visitas").hasRole("TURISTA")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/turista/lugares/*/calificacion",
                                "/api/lugares/*/calificacion").hasRole("TURISTA")
                        .requestMatchers(HttpMethod.POST,
                                "/api/turista/servicios/*/reservas").hasRole("TURISTA")
                        .requestMatchers("/api/turista/**").hasRole("TURISTA")

                        .requestMatchers(HttpMethod.GET,
                                "/api/estadisticas",
                                "/api/servicios/estadisticas",
                                "/api/servicios").hasAnyRole("PROPIETARIO", "GUIA")

                        .requestMatchers("/api/servicios/lugares", "/api/servicios/lugares/**")
                        .hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.POST,
                                "/api/servicios/lugares", "/api/servicios/lugares/**",
                                "/api/lugares", "/api/lugares/**").hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/servicios/lugares/**",
                                "/api/lugares/**").hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/servicios/lugares/**",
                                "/api/lugares/**").hasRole("PROPIETARIO")

                        .requestMatchers(HttpMethod.POST, "/api/servicios").hasAnyRole("PROPIETARIO", "GUIA")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios/**").hasAnyRole("PROPIETARIO", "GUIA")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios/**").hasAnyRole("PROPIETARIO", "GUIA")
                        .requestMatchers("/api/servicios/**").hasAnyRole("PROPIETARIO", "GUIA")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
