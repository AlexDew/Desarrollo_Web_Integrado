package com.utp.proyecto.repository;

import com.utp.proyecto.model.LugarTuristico;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LugarTuristicoRepository {
    private final ObjectMapper objectMapper;
    private final File archivo = new File("data/lugares-turisticos.json");
    private List<LugarTuristico> lugares = new ArrayList<>();

    public LugarTuristicoRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void cargarDatos() throws Exception {
        if (archivo.exists()) {
            lugares = objectMapper.readValue(archivo, new TypeReference<List<LugarTuristico>>() {});
        } else {
            lugares = objectMapper.readValue(
                    new ClassPathResource("lugares-turisticos.json").getInputStream(),
                    new TypeReference<List<LugarTuristico>>() {});
            persistir();
        }
    }

    public List<LugarTuristico> findAll() { return new ArrayList<>(lugares); }
    public LugarTuristico findById(Long id) {
        return lugares.stream().filter(l -> l.getId().equals(id)).findFirst().orElse(null);
    }
    public LugarTuristico save(LugarTuristico lugar) {
        lugares.add(lugar);
        persistir();
        return lugar;
    }
    public LugarTuristico update(LugarTuristico lugar) {
        persistir();
        return lugar;
    }
    public boolean deleteById(Long id) {
        boolean eliminado = lugares.removeIf(l -> l.getId().equals(id));
        if (eliminado) persistir();
        return eliminado;
    }
    private void persistir() {
        try {
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, lugares);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo guardar los lugares turísticos", e);
        }
    }
}
