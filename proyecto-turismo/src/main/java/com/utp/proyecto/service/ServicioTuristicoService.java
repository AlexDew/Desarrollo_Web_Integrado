package com.utp.proyecto.service;

import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.repository.ReservaRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import com.utp.proyecto.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServicioTuristicoService {
    private final ServicioTuristicoRepository servicios;
    private final UsuarioRepository usuarios;
    private final ReservaRepository reservas;

    public ServicioTuristicoService(
            ServicioTuristicoRepository servicios,
            UsuarioRepository usuarios,
            ReservaRepository reservas) {
        this.servicios = servicios;
        this.usuarios = usuarios;
        this.reservas = reservas;
    }

    public List<ServicioTuristico> listarServicios() {
        return servicios.listarActivos();
    }

    public ServicioTuristico buscarServicio(Long id) {
        return servicios.findById(id).orElse(null);
    }

    public List<ServicioTuristico> buscarServiciosPorTipo(String tipo) {
        return servicios.listarActivosPorTipo(tipo);
    }

    public List<ServicioTuristico> listarServiciosQueAceptanReservas() {
        return servicios.listarActivosQueAceptanReservas();
    }

    @Transactional
    public ServicioTuristico guardarServicio(ServicioTuristico servicio, Long creadorId) {
        servicio.setId(null);
        servicio.setActivo(true);
        servicio.setAtenciones(0);
        servicio.setCalificacion(0.0);
        servicio.setCreador(usuarios.getReferenceById(creadorId));
        return servicios.save(servicio);
    }

    @Transactional
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

    @Transactional
    public boolean eliminarServicio(Long id, Long creadorId) {
        ServicioTuristico servicio = buscarServicio(id);
        if (servicio == null || servicio.getCreador() == null
                || !servicio.getCreador().getId().equals(creadorId)) return false;
        if (reservas.existsByServicio_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un servicio que tiene solicitudes de reserva");
        }
        servicios.delete(servicio);
        return true;
    }

}
