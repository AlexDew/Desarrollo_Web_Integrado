package com.utp.proyecto.service;

import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TelefonoUnicoService {
    private final LugarTuristicoRepository lugares;
    private final ServicioTuristicoRepository servicios;

    public TelefonoUnicoService(
            LugarTuristicoRepository lugares,
            ServicioTuristicoRepository servicios) {
        this.lugares = lugares;
        this.servicios = servicios;
    }

    public void validarLugar(String contacto, Long lugarId) {
        validarUnico(contacto, lugarId, null);
    }

    public void validarServicio(String contacto, Long servicioId) {
        validarUnico(contacto, null, servicioId);
    }

    private void validarUnico(String contacto, Long lugarId, Long servicioId) {
        String telefono = normalizar(contacto);
        if (telefono == null) return;

        boolean existeEnLugar = lugares.listarContactosRegistrados().stream()
                .anyMatch(registro -> !Objects.equals(lugarId, registro.getId())
                        && telefono.equals(normalizar(registro.getContacto())));
        boolean existeEnServicio = servicios.listarContactosRegistrados().stream()
                .anyMatch(registro -> !Objects.equals(servicioId, registro.getId())
                        && telefono.equals(normalizar(registro.getContacto())));

        if (existeEnLugar || existeEnServicio) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El teléfono ya está registrado en un lugar o servicio");
        }
    }

    private String normalizar(String contacto) {
        if (contacto == null || !contacto.matches("\\+?[\\d\\s().-]+")) return null;

        String digitos = contacto.replaceAll("\\D", "");
        if (digitos.length() == 11 && digitos.startsWith("51")) {
            digitos = digitos.substring(2);
        }
        return digitos.length() == 9 ? digitos : null;
    }
}
