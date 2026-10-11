package com.utp.proyecto.config;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class CatalogoDataInitializer implements ApplicationRunner {
    private final ObjectMapper objectMapper;
    private final LugarTuristicoRepository lugares;
    private final ServicioTuristicoRepository servicios;

    public CatalogoDataInitializer(
            ObjectMapper objectMapper,
            LugarTuristicoRepository lugares,
            ServicioTuristicoRepository servicios) {
        this.objectMapper = objectMapper;
        this.lugares = lugares;
        this.servicios = servicios;
    }

    @Override
    public void run(ApplicationArguments args) {
        importarLugaresSiVacio();
        importarServiciosSiVacio();
    }

    private void importarLugaresSiVacio() {
        if (lugares.count() != 0) return;

        List<LugarTuristico> datos = leerJson(
                new File("data/lugares-turisticos.json"),
                "lugares-turisticos.json",
                new TypeReference<>() {});
        datos.forEach(lugar -> lugar.setId(null));
        lugares.saveAll(datos);
    }

    private void importarServiciosSiVacio() {
        if (servicios.count() != 0) return;

        List<ServicioTuristico> datos = leerJson(
                new File("data/servicios-turisticos.json"),
                "servicios-turisticos.json",
                new TypeReference<>() {});
        datos.forEach(servicio -> servicio.setId(null));
        servicios.saveAll(datos);
    }

    private <T> List<T> leerJson(File archivoLocal, String nombreRecurso, TypeReference<List<T>> tipo) {
        try {
            if (archivoLocal.isFile()) {
                return objectMapper.readValue(archivoLocal, tipo);
            }
            return objectMapper.readValue(new ClassPathResource(nombreRecurso).getInputStream(), tipo);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo importar el catálogo inicial: " + nombreRecurso, e);
        }
    }
}
