package com.utp.proyecto.service;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import com.utp.proyecto.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioTuristicoService {
    private final ServicioTuristicoRepository servicios;
    private final UsuarioRepository usuarios;

    public ServicioTuristicoService(ServicioTuristicoRepository servicios, UsuarioRepository usuarios) {
        this.servicios = servicios;
        this.usuarios = usuarios;
    }

    public List<ServicioTuristico> listarServicios() {
        return servicios.findAll().stream().filter(ServicioTuristico::getActivo).toList();
    }

    public ServicioTuristico buscarServicio(Long id) {
        return servicios.findById(id).orElse(null);
    }

    public List<ServicioTuristico> buscarServiciosPorTipo(String tipo) {
        return listarServicios().stream()
                .filter(servicio -> servicio.getTipo().equalsIgnoreCase(tipo))
                .toList();
    }

    public ServicioTuristico guardarServicio(ServicioTuristico servicio, Long creadorId) {
        servicio.setId(null);
        servicio.setActivo(true);
        servicio.setAtenciones(0);
        servicio.setCalificacion(0.0);
        servicio.setCreador(usuarios.getReferenceById(creadorId));
        return servicios.save(servicio);
    }

    public ServicioTuristico actualizarServicio(Long id, ServicioTuristico datos, Long creadorId) {
        ServicioTuristico servicio = buscarServicio(id);
        if (servicio == null || servicio.getCreador() == null
                || !servicio.getCreador().getId().equals(creadorId)) return null;
        servicio.setNombre(datos.getNombre());
        servicio.setTipo(datos.getTipo());
        servicio.setDescripcion(datos.getDescripcion());
        servicio.setHorarios(datos.getHorarios());
        servicio.setPrecio(datos.getPrecio());
        servicio.setRuta(datos.getRuta());
        servicio.setContacto(datos.getContacto());
        servicio.setAceptaReservas(datos.getAceptaReservas());
        servicio.setActivo(datos.getActivo() == null || datos.getActivo());
        return servicios.save(servicio);
    }

    public boolean eliminarServicio(Long id, Long creadorId) {
        ServicioTuristico servicio = buscarServicio(id);
        if (servicio == null || servicio.getCreador() == null
                || !servicio.getCreador().getId().equals(creadorId)) return false;
        servicios.delete(servicio);
        return true;
    }

    public ServicioTuristico registrarAtencion(Long id) {
        ServicioTuristico servicio = buscarServicio(id);
        if (servicio == null) return null;
        servicio.setAtenciones(servicio.getAtenciones() + 1);
        return servicios.save(servicio);
    }
}
