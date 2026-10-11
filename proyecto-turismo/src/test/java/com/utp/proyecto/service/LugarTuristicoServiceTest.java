package com.utp.proyecto.service;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LugarTuristicoServiceTest {
    @Mock
    private LugarTuristicoRepository lugares;
    @Mock
    private UsuarioRepository usuarios;
    @InjectMocks
    private LugarTuristicoService lugaresService;

    @Test
    void guardarLugar_asignaValoresIniciales() {
        LugarTuristico nuevo = new LugarTuristico();
        Usuario propietario = new Usuario();
        propietario.setId(7L);
        when(usuarios.getReferenceById(7L)).thenReturn(propietario);
        when(lugares.save(nuevo)).thenReturn(nuevo);

        LugarTuristico resultado = lugaresService.guardarLugar(nuevo, 7L);

        assertThat(resultado.getId()).isNull();
        assertThat(resultado.getVisitantes()).isZero();
        assertThat(resultado.getActivo()).isTrue();
        assertThat(resultado.getPropietario()).isSameAs(propietario);
        verify(lugares).save(nuevo);
    }

    @Test
    void registrarVisita_incrementaContador() {
        LugarTuristico lugar = new LugarTuristico();
        lugar.setId(1L);
        lugar.setVisitantes(4);
        when(lugares.findById(1L)).thenReturn(Optional.of(lugar));
        when(lugares.save(lugar)).thenReturn(lugar);

        lugaresService.registrarVisita(1L);

        assertThat(lugar.getVisitantes()).isEqualTo(5);
        verify(lugares).save(lugar);
    }
}
