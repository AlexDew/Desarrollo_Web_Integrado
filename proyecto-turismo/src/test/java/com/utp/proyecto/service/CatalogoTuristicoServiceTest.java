package com.utp.proyecto.service;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoTuristicoServiceTest {
    @Mock
    private LugarTuristicoRepository lugares;
    @Mock
    private ServicioTuristicoRepository servicios;
    @InjectMocks
    private CatalogoTuristicoService catalogo;

    @Test
    void guardarLugar_asignaIdYValoresIniciales() {
        when(lugares.findAll()).thenReturn(List.of());
        LugarTuristico nuevo = new LugarTuristico();
        when(lugares.save(nuevo)).thenReturn(nuevo);

        LugarTuristico resultado = catalogo.guardarLugar(nuevo);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getVisitantes()).isZero();
        assertThat(resultado.getActivo()).isTrue();
        verify(lugares).save(nuevo);
    }

    @Test
    void registrarVisita_incrementaContador() {
        LugarTuristico lugar = new LugarTuristico();
        lugar.setId(1L);
        lugar.setVisitantes(4);
        when(lugares.findById(1L)).thenReturn(lugar);
        when(lugares.update(lugar)).thenReturn(lugar);

        catalogo.registrarVisita(1L);

        assertThat(lugar.getVisitantes()).isEqualTo(5);
        verify(lugares).update(lugar);
    }
}
