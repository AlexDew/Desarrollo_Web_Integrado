package com.utp.proyecto.controller;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.Rol;
import com.utp.proyecto.security.UsuarioAutenticado;
import com.utp.proyecto.service.LugarTuristicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LugaresControllerTest {
    private static final UsuarioAutenticado PROPIETARIO =
            new UsuarioAutenticado(7L, "propietario@example.test", "hash", true, Set.of(Rol.PROPIETARIO));

    @Mock
    private LugarTuristicoService lugares;
    @InjectMocks
    private LugaresController controller;

    @Test
    void eliminarLugar_inexistenteDevuelveNotFound() {
        when(lugares.eliminarLugar(99L, PROPIETARIO.getId())).thenReturn(false);

        assertThat(controller.eliminarLugar(99L, PROPIETARIO).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void actualizarLugar_existenteDevuelveOk() {
        LugarTuristico datos = new LugarTuristico();
        when(lugares.actualizarLugar(1L, datos, PROPIETARIO.getId())).thenReturn(datos);

        assertThat(controller.actualizarLugar(1L, datos, PROPIETARIO).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void listarLugares_filtraPorTipo() {
        LugarTuristico lugar = new LugarTuristico();
        lugar.setTipo("naturaleza");
        when(lugares.buscarLugaresPorTipo("naturaleza")).thenReturn(List.of(lugar));

        assertThat(controller.listarLugares("naturaleza")).containsExactly(lugar);
    }

    @Test
    void calificar_valorInvalidoDevuelveBadRequest() {
        when(lugares.calificarLugar(1L, 6.0)).thenReturn(false);

        assertThat(controller.calificar(1L, 6.0).getStatusCode().value()).isEqualTo(400);
    }
}
