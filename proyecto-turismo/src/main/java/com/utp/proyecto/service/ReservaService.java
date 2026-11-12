package com.utp.proyecto.service;

import com.utp.proyecto.dto.CrearReservaRequest;
import com.utp.proyecto.dto.ReservaResponse;
import com.utp.proyecto.model.EstadoReserva;
import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.Reserva;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.ReservaRepository;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import com.utp.proyecto.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReservaService {
    private final ReservaRepository reservas;
    private final ServicioTuristicoRepository servicios;
    private final LugarTuristicoRepository lugares;
    private final UsuarioRepository usuarios;

    public ReservaService(
            ReservaRepository reservas,
            ServicioTuristicoRepository servicios,
            LugarTuristicoRepository lugares,
            UsuarioRepository usuarios) {
        this.reservas = reservas;
        this.servicios = servicios;
        this.lugares = lugares;
        this.usuarios = usuarios;
    }

    @Transactional
    public ReservaResponse crear(Long servicioId, Long turistaId, CrearReservaRequest request) {
        ServicioTuristico servicio = servicios.findById(servicioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el servicio"));
        if (!Boolean.TRUE.equals(servicio.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El servicio no está disponible para recibir solicitudes de reserva");
        }

        Reserva reserva = new Reserva();
        reserva.setServicio(servicio);
        reserva.setTurista(usuarios.getReferenceById(turistaId));
        return guardarSolicitud(reserva, request);
    }

    @Transactional
    public ReservaResponse crearParaLugar(Long lugarId, Long turistaId, CrearReservaRequest request) {
        LugarTuristico lugar = lugares.findById(lugarId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el lugar"));
        if (!Boolean.TRUE.equals(lugar.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El lugar no está disponible para recibir solicitudes de reserva");
        }

        Reserva reserva = new Reserva();
        reserva.setLugar(lugar);
        reserva.setTurista(usuarios.getReferenceById(turistaId));
        return guardarSolicitud(reserva, request);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarDelTurista(Long turistaId) {
        return reservas.listarDelTurista(turistaId).stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarDeServiciosDelPropietario(Long propietarioId) {
        return reservas.listarDeServiciosDelPropietario(propietarioId).stream().map(this::convertir).toList();
    }

    @Transactional
    public ReservaResponse cambiarEstado(Long reservaId, Long propietarioId, EstadoReserva nuevoEstado) {
        if (nuevoEstado != EstadoReserva.CONFIRMADA && nuevoEstado != EstadoReserva.RECHAZADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El propietario solo puede confirmar o rechazar una solicitud");
        }

        Reserva reserva = reservas.buscarConPropietarioYTurista(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva"));
        Long creadorId = reserva.getServicio() != null
                ? reserva.getServicio().getCreador() == null ? null : reserva.getServicio().getCreador().getId()
                : reserva.getLugar().getPropietario() == null ? null : reserva.getLugar().getPropietario().getId();
        if (!java.util.Objects.equals(creadorId, propietarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden confirmar o rechazar solicitudes pendientes");
        }

        reserva.setEstado(nuevoEstado);
        return convertir(reservas.save(reserva));
    }

    @Transactional
    public void cancelar(Long reservaId, Long turistaId) {
        Reserva reserva = reservas.buscarDelTurista(reservaId, turistaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva"));
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden cancelar solicitudes pendientes");
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        reservas.save(reserva);
    }

    private ReservaResponse convertir(Reserva reserva) {
        ServicioTuristico servicio = reserva.getServicio();
        LugarTuristico lugar = reserva.getLugar();
        Usuario turista = reserva.getTurista();
        return new ReservaResponse(
                reserva.getId(),
                servicio == null ? null : servicio.getId(),
                servicio == null ? null : servicio.getNombre(),
                turista.getId(),
                turista.getNombre(),
                turista.getEmail(),
                reserva.getFecha(),
                reserva.getCantidadPersonas(),
                reserva.getEstado(),
                lugar == null ? null : lugar.getId(),
                lugar == null ? null : lugar.getNombre());
    }

    private ReservaResponse guardarSolicitud(Reserva reserva, CrearReservaRequest request) {
        reserva.setFecha(request.fecha());
        reserva.setCantidadPersonas(request.cantidadPersonas());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        return convertir(reservas.save(reserva));
    }
}
