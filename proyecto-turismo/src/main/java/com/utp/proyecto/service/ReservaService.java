package com.utp.proyecto.service;

import com.utp.proyecto.dto.CrearReservaRequest;
import com.utp.proyecto.dto.ReservaResponse;
import com.utp.proyecto.model.EstadoReserva;
import com.utp.proyecto.model.Reserva;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.ReservaRepository;
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
    private final UsuarioRepository usuarios;

    public ReservaService(
            ReservaRepository reservas,
            ServicioTuristicoRepository servicios,
            UsuarioRepository usuarios) {
        this.reservas = reservas;
        this.servicios = servicios;
        this.usuarios = usuarios;
    }

    @Transactional
    public ReservaResponse crear(Long servicioId, Long turistaId, CrearReservaRequest request) {
        ServicioTuristico servicio = servicios.findById(servicioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el servicio"));
        if (!Boolean.TRUE.equals(servicio.getActivo())
                || !Boolean.TRUE.equals(servicio.getAceptaReservas())
                || servicio.getCreador() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El servicio no está disponible para recibir solicitudes de reserva");
        }

        Reserva reserva = new Reserva();
        reserva.setServicio(servicio);
        reserva.setTurista(usuarios.getReferenceById(turistaId));
        reserva.setFecha(request.fecha());
        reserva.setCantidadPersonas(request.cantidadPersonas());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        return convertir(reservas.save(reserva));
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
        if (reserva.getServicio().getCreador() == null
                || !reserva.getServicio().getCreador().getId().equals(propietarioId)) {
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
        Usuario turista = reserva.getTurista();
        return new ReservaResponse(
                reserva.getId(),
                servicio.getId(),
                servicio.getNombre(),
                turista.getId(),
                turista.getNombre(),
                turista.getEmail(),
                reserva.getFecha(),
                reserva.getCantidadPersonas(),
                reserva.getEstado());
    }
}
