package com.utp.proyecto.repository;

import com.utp.proyecto.model.ServicioTuristico;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ServicioTuristicoRepository {
    private final ObjectMapper objectMapper;
    private final File archivo = new File("data/servicios-turisticos.json");
    private List<ServicioTuristico> servicios = new ArrayList<>();

    public ServicioTuristicoRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void cargarDatos() throws Exception {
        if (archivo.exists()) {
            servicios = objectMapper.readValue(archivo, new TypeReference<List<ServicioTuristico>>() {});
        } else {
            servicios = objectMapper.readValue(
                    new ClassPathResource("servicios-turisticos.json").getInputStream(),
                    new TypeReference<List<ServicioTuristico>>() {});
            persistir();
        }
    }

    public List<ServicioTuristico> findAll() { return new ArrayList<>(servicios); }
    public ServicioTuristico findById(Long id) {
        return servicios.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
    public ServicioTuristico save(ServicioTuristico servicio) {
        servicios.add(servicio);
        persistir();
        return servicio;
    }
    public ServicioTuristico update(ServicioTuristico servicio) {
        persistir();
        return servicio;
    }
    public boolean deleteById(Long id) {
        boolean eliminado = servicios.removeIf(s -> s.getId().equals(id));
        if (eliminado) persistir();
        return eliminado;
    }
    private void persistir() {
        try {
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, servicios);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo guardar los servicios turísticos", e);
        }
    }
}
