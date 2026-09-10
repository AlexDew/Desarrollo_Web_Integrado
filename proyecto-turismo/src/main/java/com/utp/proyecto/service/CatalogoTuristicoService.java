package com.utp.proyecto.service;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoTuristicoService {
    private final LugarTuristicoRepository lugares;
    private final ServicioTuristicoRepository servicios;

    public CatalogoTuristicoService(LugarTuristicoRepository lugares, ServicioTuristicoRepository servicios) {
        this.lugares = lugares;
        this.servicios = servicios;
    }

    public List<LugarTuristico> listarLugares() {
        return lugares.findAll().stream().filter(LugarTuristico::getActivo).toList();
    }
    public LugarTuristico buscarLugar(Long id) { return lugares.findById(id); }
    public List<LugarTuristico> buscarLugaresPorTipo(String tipo) {
        return listarLugares().stream().filter(l -> l.getTipo().equalsIgnoreCase(tipo)).toList();
    }
    public LugarTuristico guardarLugar(LugarTuristico lugar) {
        lugar.setId(siguienteIdLugar());
        lugar.setActivo(true);
        lugar.setVisitantes(0);
        lugar.setCalificacion(0.0);
        return lugares.save(lugar);
    }
    public LugarTuristico actualizarLugar(Long id, LugarTuristico datos) {
        LugarTuristico lugar = buscarLugar(id);
        if (lugar == null) return null;
        lugar.setNombre(datos.getNombre());
        lugar.setTipo(datos.getTipo());
        lugar.setDescripcion(datos.getDescripcion());
        lugar.setDireccion(datos.getDireccion());
        lugar.setHorarios(datos.getHorarios());
        lugar.setContacto(datos.getContacto());
        lugar.setActivo(datos.getActivo() == null || datos.getActivo());
        return lugares.update(lugar);
    }
    public boolean eliminarLugar(Long id) { return lugares.deleteById(id); }

    public List<ServicioTuristico> listarServicios() {
        return servicios.findAll().stream().filter(ServicioTuristico::getActivo).toList();
    }
    public ServicioTuristico buscarServicio(Long id) { return servicios.findById(id); }
    public List<ServicioTuristico> buscarServiciosPorTipo(String tipo) {
        return listarServicios().stream().filter(s -> s.getTipo().equalsIgnoreCase(tipo)).toList();
    }
    public ServicioTuristico guardarServicio(ServicioTuristico servicio) {
        servicio.setId(siguienteIdServicio());
        servicio.setActivo(true);
        servicio.setAtenciones(0);
        servicio.setCalificacion(0.0);
        return servicios.save(servicio);
    }
    public ServicioTuristico actualizarServicio(Long id, ServicioTuristico datos) {
        ServicioTuristico servicio = buscarServicio(id);
        if (servicio == null) return null;
        servicio.setNombre(datos.getNombre());
        servicio.setTipo(datos.getTipo());
        servicio.setDescripcion(datos.getDescripcion());
        servicio.setHorarios(datos.getHorarios());
        servicio.setPrecio(datos.getPrecio());
        servicio.setRuta(datos.getRuta());
        servicio.setContacto(datos.getContacto());
        servicio.setAceptaReservas(datos.getAceptaReservas());
        servicio.setActivo(datos.getActivo() == null || datos.getActivo());
        return servicios.update(servicio);
    }
    public boolean eliminarServicio(Long id) { return servicios.deleteById(id); }

    public LugarTuristico registrarVisita(Long id) {
        LugarTuristico lugar = buscarLugar(id);
        if (lugar == null) return null;
        lugar.setVisitantes(lugar.getVisitantes() + 1);
        return lugares.update(lugar);
    }
    public ServicioTuristico registrarAtencion(Long id) {
        ServicioTuristico servicio = buscarServicio(id);
        if (servicio == null) return null;
        servicio.setAtenciones(servicio.getAtenciones() + 1);
        return servicios.update(servicio);
    }
    public boolean calificarLugar(Long id, Double calificacion) {
        LugarTuristico lugar = buscarLugar(id);
        if (lugar == null || calificacion == null || calificacion < 1 || calificacion > 5) return false;
        lugar.setCalificacion(calificacion);
        lugares.update(lugar);
        return true;
    }
    private long siguienteIdLugar() { return lugares.findAll().stream().mapToLong(LugarTuristico::getId).max().orElse(0) + 1; }
    private long siguienteIdServicio() { return servicios.findAll().stream().mapToLong(ServicioTuristico::getId).max().orElse(0) + 1; }
}
