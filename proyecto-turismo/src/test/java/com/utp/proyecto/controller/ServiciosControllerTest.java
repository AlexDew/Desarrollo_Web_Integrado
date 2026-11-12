package com.utp.proyecto.controller;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.model.Rol;
import com.utp.proyecto.security.UsuarioAutenticado;
import com.utp.proyecto.service.ServicioTuristicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiciosControllerTest {
    private static final UsuarioAutenticado PROPIETARIO =
            new UsuarioAutenticado(7L, "propietario@example.test", "hash", true, Set.of(Rol.PROPIETARIO));

    @Mock
    private ServicioTuristicoService servicios;
    @InjectMocks
    private ServiciosController controller;

    @Test
    void registrarServicio_devuelveCreated() {
        ServicioTuristico servicio = new ServicioTuristico();
        when(servicios.guardarServicio(servicio, PROPIETARIO.getId())).thenReturn(servicio);

        assertThat(controller.registrarServicio(servicio, PROPIETARIO)).isSameAs(servicio);
    }

}
