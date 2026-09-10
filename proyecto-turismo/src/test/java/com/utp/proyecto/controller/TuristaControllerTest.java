package com.utp.proyecto.controller;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.CatalogoTuristicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TuristaControllerTest {
    @Mock
    private CatalogoTuristicoService catalogo;
    @InjectMocks
    private TuristaController controller;

    @Test
    void listarLugares_filtraPorTipo() {
        LugarTuristico lugar = new LugarTuristico();
        lugar.setTipo("naturaleza");
        when(catalogo.buscarLugaresPorTipo("naturaleza")).thenReturn(List.of(lugar));

        assertThat(controller.lugares("naturaleza")).containsExactly(lugar);
    }

    @Test
    void reservar_lugarExistenteDevuelveRespuestaOk() {
        ServicioTuristico servicio = new ServicioTuristico();
        servicio.setId(1L);
        when(catalogo.registrarAtencion(1L)).thenReturn(servicio);

        assertThat(controller.reservar(1L).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void calificar_valorInvalidoDevuelveBadRequest() {
        when(catalogo.calificarLugar(1L, 6.0)).thenReturn(false);

        assertThat(controller.calificar(1L, 6.0).getStatusCode().value()).isEqualTo(400);
    }
}
