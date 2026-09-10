package com.utp.proyecto.controller;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.CatalogoTuristicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiciosControllerTest {
    @Mock
    private CatalogoTuristicoService catalogo;
    @InjectMocks
    private ServiciosController controller;

    @Test
    void registrarServicio_devuelveCreated() {
        ServicioTuristico servicio = new ServicioTuristico();
        when(catalogo.guardarServicio(servicio)).thenReturn(servicio);

        assertThat(controller.registrarServicio(servicio)).isSameAs(servicio);
    }

    @Test
    void eliminarLugar_inexistenteDevuelveNotFound() {
        when(catalogo.eliminarLugar(99L)).thenReturn(false);

        assertThat(controller.eliminarLugar(99L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void actualizarLugar_existenteDevuelveOk() {
        LugarTuristico datos = new LugarTuristico();
        when(catalogo.actualizarLugar(1L, datos)).thenReturn(datos);

        assertThat(controller.actualizarLugar(1L, datos).getStatusCode().value()).isEqualTo(200);
    }
}
