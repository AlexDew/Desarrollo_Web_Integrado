package com.utp.proyecto.repository;

import com.utp.proyecto.model.ServicioTuristico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ServicioTuristicoRepositoryTest {
    @Autowired
    private ServicioTuristicoRepository servicios;

    @Test
    void listarActivos_excluyeServiciosInactivos() {
        String tipo = tipoUnico();
        servicios.saveAndFlush(crearServicio("JPQL Guía", tipo, true, true));
        servicios.saveAndFlush(crearServicio("JPQL Transporte", tipo, true, false));
        servicios.saveAndFlush(crearServicio("JPQL Inactivo", tipo, false, true));

        List<ServicioTuristico> resultado = servicios.listarActivos();

        assertThat(resultado).extracting(ServicioTuristico::getNombre)
                .contains("JPQL Guía", "JPQL Transporte")
                .doesNotContain("JPQL Inactivo");
        assertThat(resultado).filteredOn(servicio -> servicio.getNombre().startsWith("JPQL "))
                .allMatch(ServicioTuristico::getActivo);
    }

    @Test
    void listarActivosPorTipo_noDistingueMayusculasYExcluyeInactivos() {
        String tipo = tipoUnico();
        servicios.saveAndFlush(crearServicio("JPQL Guía", tipo, true, true));
        servicios.saveAndFlush(crearServicio("JPQL Guía inactiva", tipo, false, true));
        servicios.saveAndFlush(crearServicio("JPQL Transporte", tipoUnico(), true, false));

        List<ServicioTuristico> resultado = servicios.listarActivosPorTipo(tipo.toUpperCase());

        assertThat(resultado).extracting(ServicioTuristico::getNombre)
                .containsExactly("JPQL Guía");
    }

    @Test
    void listarActivosQueAceptanReservas_filtraPorEstadoYDisponibilidad() {
        String tipo = tipoUnico();
        servicios.saveAndFlush(crearServicio("JPQL Disponible", tipo, true, true));
        servicios.saveAndFlush(crearServicio("JPQL Sin reservas", tipo, true, false));
        servicios.saveAndFlush(crearServicio("JPQL Inactivo disponible", tipo, false, true));

        List<ServicioTuristico> resultado = servicios.listarActivosQueAceptanReservas();

        assertThat(resultado).extracting(ServicioTuristico::getNombre)
                .contains("JPQL Disponible")
                .doesNotContain("JPQL Sin reservas", "JPQL Inactivo disponible");
        assertThat(resultado).allMatch(servicio ->
                Boolean.TRUE.equals(servicio.getActivo()) && Boolean.TRUE.equals(servicio.getAceptaReservas()));
    }

    private ServicioTuristico crearServicio(String nombre, String tipo, boolean activo, boolean aceptaReservas) {
        ServicioTuristico servicio = new ServicioTuristico();
        servicio.setNombre(nombre);
        servicio.setTipo(tipo);
        servicio.setDescripcion("Descripción de prueba");
        servicio.setHorarios("Todos los días");
        servicio.setPrecio(new BigDecimal("25.00"));
        servicio.setRuta("Piura");
        servicio.setContacto("contacto@example.test");
        servicio.setAceptaReservas(aceptaReservas);
        servicio.setAtenciones(0);
        servicio.setCalificacion(0.0);
        servicio.setActivo(activo);
        return servicio;
    }

    private String tipoUnico() {
        return "jpql-" + UUID.randomUUID();
    }
}
