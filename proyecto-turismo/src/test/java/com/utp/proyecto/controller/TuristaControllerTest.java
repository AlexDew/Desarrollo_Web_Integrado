package com.utp.proyecto.controller;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.ServicioTuristicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TuristaControllerTest {
    @Mock
    private ServicioTuristicoService servicios;
    @InjectMocks
    private TuristaController controller;

    @Test
    void servicios_sinFiltroListaCatalogoActivo() {
        ServicioTuristico servicio = new ServicioTuristico();
        when(servicios.listarServicios()).thenReturn(List.of(servicio));

        assertThat(controller.servicios(null)).containsExactly(servicio);
        verify(servicios).listarServicios();
    }

    @Test
    void servicios_conFiltroBuscaPorTipo() {
        ServicioTuristico servicio = new ServicioTuristico();
        when(servicios.buscarServiciosPorTipo("guia")).thenReturn(List.of(servicio));

        assertThat(controller.servicios("guia")).containsExactly(servicio);
        verify(servicios).buscarServiciosPorTipo("guia");
    }

}
