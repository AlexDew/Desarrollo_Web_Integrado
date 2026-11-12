package com.utp.proyecto.repository;

import com.utp.proyecto.model.LugarTuristico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LugarTuristicoRepositoryTest {
    @Autowired
    private LugarTuristicoRepository lugares;

    @Test
    void listarActivosPorTipo_ignoraMayusculasYExcluyeInactivos() {
        String tipo = tipoUnico();
        lugares.saveAndFlush(crearLugar("JPQL activo", tipo, 3.0, 12, true));
        lugares.saveAndFlush(crearLugar("JPQL inactivo", tipo, 5.0, 20, false));
        lugares.saveAndFlush(crearLugar("JPQL otro tipo", tipoUnico(), 4.0, 15, true));

        List<LugarTuristico> resultado = lugares.listarActivosPorTipo(tipo.toUpperCase());

        assertThat(resultado).extracting(LugarTuristico::getNombre)
                .containsExactly("JPQL activo");
    }

    @Test
    void listarActivosMejorCalificados_ordenaDescendenteYExcluyeInactivos() {
        lugares.saveAndFlush(crearLugar("JPQL calificacion alta", tipoUnico(), 100.0, 1, true));
        lugares.saveAndFlush(crearLugar("JPQL calificacion baja", tipoUnico(), 99.0, 1, true));
        lugares.saveAndFlush(crearLugar("JPQL calificacion inactiva", tipoUnico(), 101.0, 1, false));

        List<LugarTuristico> resultado = lugares.listarActivosMejorCalificados();
        List<String> nombres = resultado.stream().map(LugarTuristico::getNombre).toList();

        assertThat(nombres.indexOf("JPQL calificacion alta"))
                .isLessThan(nombres.indexOf("JPQL calificacion baja"));
        assertThat(nombres).doesNotContain("JPQL calificacion inactiva");
        assertThat(resultado).allMatch(LugarTuristico::getActivo);
    }

    @Test
    void listarActivosMasVisitados_ordenaDescendenteYExcluyeInactivos() {
        lugares.saveAndFlush(crearLugar("JPQL visitas altas", tipoUnico(), 1.0, Integer.MAX_VALUE, true));
        lugares.saveAndFlush(crearLugar("JPQL visitas bajas", tipoUnico(), 1.0, Integer.MAX_VALUE - 1, true));
        lugares.saveAndFlush(crearLugar("JPQL visitas inactivas", tipoUnico(), 1.0, Integer.MAX_VALUE, false));

        List<LugarTuristico> resultado = lugares.listarActivosMasVisitados();
        List<String> nombres = resultado.stream().map(LugarTuristico::getNombre).toList();

        assertThat(nombres.indexOf("JPQL visitas altas"))
                .isLessThan(nombres.indexOf("JPQL visitas bajas"));
        assertThat(nombres).doesNotContain("JPQL visitas inactivas");
        assertThat(resultado).allMatch(LugarTuristico::getActivo);
    }

    private LugarTuristico crearLugar(String nombre, String tipo, double calificacion, int visitantes, boolean activo) {
        LugarTuristico lugar = new LugarTuristico();
        lugar.setNombre(nombre);
        lugar.setTipo(tipo);
        lugar.setDescripcion("Descripción de prueba");
        lugar.setDireccion("Piura");
        lugar.setHorarios("Todos los días");
        lugar.setContacto("contacto@example.test");
        lugar.setCalificacion(calificacion);
        lugar.setVisitantes(visitantes);
        lugar.setActivo(activo);
        return lugar;
    }

    private String tipoUnico() {
        return "jpql-" + UUID.randomUUID();
    }
}
