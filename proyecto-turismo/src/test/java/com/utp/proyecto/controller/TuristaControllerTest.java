package com.utp.proyecto.controller;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.ServicioTuristicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TuristaControllerTest {
    @Mock
    private ServicioTuristicoService servicios;
    @InjectMocks
    private TuristaController controller;

    @Test
    void reservar_lugarExistenteDevuelveRespuestaOk() {
        ServicioTuristico servicio = new ServicioTuristico();
        servicio.setId(1L);
        when(servicios.registrarAtencion(1L)).thenReturn(servicio);

        assertThat(controller.reservar(1L).getStatusCode().value()).isEqualTo(200);
    }
}
