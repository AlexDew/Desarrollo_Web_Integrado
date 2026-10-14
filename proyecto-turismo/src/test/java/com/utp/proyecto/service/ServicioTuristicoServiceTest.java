package com.utp.proyecto.service;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.ReservaRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import com.utp.proyecto.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioTuristicoServiceTest {
    @Mock
    private ServicioTuristicoRepository servicios;
    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private ReservaRepository reservas;
    @InjectMocks
    private ServicioTuristicoService serviciosService;

    @Test
    void guardarServicio_asignaValoresIniciales() {
        ServicioTuristico nuevo = new ServicioTuristico();
        Usuario creador = new Usuario();
        creador.setId(7L);
        when(usuarios.getReferenceById(7L)).thenReturn(creador);
        when(servicios.save(nuevo)).thenReturn(nuevo);

        ServicioTuristico resultado = serviciosService.guardarServicio(nuevo, 7L);

        assertThat(resultado.getId()).isNull();
        assertThat(resultado.getAtenciones()).isZero();
        assertThat(resultado.getActivo()).isTrue();
        assertThat(resultado.getCreador()).isSameAs(creador);
        verify(servicios).save(nuevo);
    }

    @Test
    void listarYBuscarServicios_deleganConsultasAlRepositorio() {
        serviciosService.listarServicios();
        serviciosService.buscarServiciosPorTipo("guia");
        serviciosService.listarServiciosQueAceptanReservas();

        verify(servicios).listarActivos();
        verify(servicios).listarActivosPorTipo("guia");
        verify(servicios).listarActivosQueAceptanReservas();
        verifyNoMoreInteractions(servicios);
    }

    @Test
    void metodosDeEscrituraEstanMarcadosComoTransaccionales() throws NoSuchMethodException {
        assertThat(ServicioTuristicoService.class
                .getMethod("guardarServicio", ServicioTuristico.class, Long.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(ServicioTuristicoService.class
                .getMethod("actualizarServicio", Long.class, ServicioTuristico.class, Long.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(ServicioTuristicoService.class
                .getMethod("eliminarServicio", Long.class, Long.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }
}
