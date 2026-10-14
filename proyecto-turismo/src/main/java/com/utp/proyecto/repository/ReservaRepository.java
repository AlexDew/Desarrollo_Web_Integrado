package com.utp.proyecto.repository;

import com.utp.proyecto.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByServicio_Id(Long servicioId);

    @Query("""
            SELECT reserva
            FROM Reserva reserva
            JOIN FETCH reserva.servicio servicio
            JOIN FETCH reserva.turista turista
            WHERE turista.id = :turistaId
            ORDER BY reserva.fecha DESC, reserva.id DESC
            """)
    List<Reserva> listarDelTurista(@Param("turistaId") Long turistaId);

    @Query("""
            SELECT reserva
            FROM Reserva reserva
            JOIN FETCH reserva.servicio servicio
            JOIN FETCH reserva.turista turista
            WHERE servicio.creador.id = :propietarioId
            ORDER BY reserva.fecha DESC, reserva.id DESC
            """)
    List<Reserva> listarDeServiciosDelPropietario(@Param("propietarioId") Long propietarioId);

    @Query("""
            SELECT reserva
            FROM Reserva reserva
            JOIN FETCH reserva.servicio servicio
            JOIN FETCH reserva.turista turista
            WHERE reserva.id = :reservaId
              AND turista.id = :turistaId
            """)
    Optional<Reserva> buscarDelTurista(
            @Param("reservaId") Long reservaId,
            @Param("turistaId") Long turistaId);

    @Query("""
            SELECT reserva
            FROM Reserva reserva
            JOIN FETCH reserva.servicio servicio
            JOIN FETCH servicio.creador creador
            JOIN FETCH reserva.turista turista
            WHERE reserva.id = :reservaId
            """)
    Optional<Reserva> buscarConPropietarioYTurista(@Param("reservaId") Long reservaId);
}
