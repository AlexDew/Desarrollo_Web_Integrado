package com.utp.proyecto.service;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.repository.UsuarioRepository;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LugarTuristicoService {
    private final LugarTuristicoRepository lugares;
    private final UsuarioRepository usuarios;
    private final TelefonoUnicoService telefonos;
    private final ReservaRepository reservas;

    public LugarTuristicoService(
            LugarTuristicoRepository lugares,
            UsuarioRepository usuarios,
            TelefonoUnicoService telefonos,
            ReservaRepository reservas) {
        this.lugares = lugares;
        this.usuarios = usuarios;
        this.telefonos = telefonos;
        this.reservas = reservas;
    }

    public List<LugarTuristico> listarLugares() {
        return lugares.findAll().stream()
                .filter(lugar -> Boolean.TRUE.equals(lugar.getActivo()))
                .toList();
    }

    public List<LugarTuristico> listarLugaresDelPropietario(Long propietarioId) {
        return lugares.findByPropietario_IdAndActivoTrue(propietarioId);
    }

    public LugarTuristico buscarLugar(Long id) {
        return lugares.findById(id).orElse(null);
    }

    public List<LugarTuristico> buscarLugaresPorTipo(String tipo) {
        return lugares.listarActivosPorTipo(tipo);
    }

    public List<LugarTuristico> listarActivosMejorCalificados() {
        return lugares.listarActivosMejorCalificados();
    }

    public List<LugarTuristico> listarActivosMasVisitados() {
        return lugares.listarActivosMasVisitados();
    }

    public LugarTuristico guardarLugar(LugarTuristico lugar, Long propietarioId) {
        telefonos.validarLugar(lugar.getContacto(), null);
        lugar.setId(null);
        lugar.setActivo(true);
        lugar.setVisitantes(0);
        lugar.setCalificacion(0.0);
        lugar.setPropietario(usuarios.getReferenceById(propietarioId));
        return lugares.save(lugar);
    }

    public LugarTuristico actualizarLugar(Long id, LugarTuristico datos, Long propietarioId) {
        LugarTuristico lugar = buscarLugar(id);
        if (lugar == null || lugar.getPropietario() == null
                || !lugar.getPropietario().getId().equals(propietarioId)) return null;
        telefonos.validarLugar(datos.getContacto(), id);
        lugar.setNombre(datos.getNombre());
        lugar.setTipo(datos.getTipo());
        lugar.setDescripcion(datos.getDescripcion());
        lugar.setDireccion(datos.getDireccion());
        lugar.setHorarios(datos.getHorarios());
        lugar.setContacto(datos.getContacto());
        lugar.setActivo(datos.getActivo() == null || datos.getActivo());
        return lugares.save(lugar);
    }

    public boolean eliminarLugar(Long id, Long propietarioId) {
        LugarTuristico lugar = buscarLugar(id);
        if (lugar == null || lugar.getPropietario() == null
                || !lugar.getPropietario().getId().equals(propietarioId)) return false;
        if (reservas.existsByLugar_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un lugar que tiene solicitudes de reserva");
        }
        lugares.delete(lugar);
        return true;
    }

    @Transactional
    public LugarTuristico registrarVisita(Long id) {
        LugarTuristico lugar = buscarLugar(id);
        if (lugar == null) return null;
        lugar.setVisitantes(lugar.getVisitantes() + 1);
        return lugares.save(lugar);
    }

    @Transactional
    public boolean calificarLugar(Long id, Double calificacion) {
        LugarTuristico lugar = buscarLugar(id);
        if (lugar == null || calificacion == null || calificacion < 1 || calificacion > 5) {
            return false;
        }
        lugar.setCalificacion(calificacion);
        lugares.save(lugar);
        return true;
    }
}
