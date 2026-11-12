package com.utp.proyecto.repository;

import com.utp.proyecto.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByServicio_Id(Long servicioId);
    boolean existsByLugar_Id(Long lugarId);

    @Query("""
            SELECT reserva
            FROM Reserva reserva
            LEFT JOIN FETCH reserva.servicio servicio
            LEFT JOIN FETCH reserva.lugar lugar
            JOIN FETCH reserva.turista turista
            WHERE turista.id = :turistaId
            ORDER BY reserva.fecha DESC, reserva.id DESC
            """)
    List<Reserva> listarDelTurista(@Param("turistaId") Long turistaId);

    @Query("""
            SELECT reserva
            FROM Reserva reserva
            LEFT JOIN FETCH reserva.servicio servicio
            LEFT JOIN FETCH servicio.creador servicioCreador
            LEFT JOIN FETCH reserva.lugar lugar
            LEFT JOIN FETCH lugar.propietario lugarPropietario
            JOIN FETCH reserva.turista turista
            WHERE servicioCreador.id = :propietarioId
               OR lugarPropietario.id = :propietarioId
            ORDER BY reserva.fecha DESC, reserva.id DESC
            """)
    List<Reserva> listarDeServiciosDelPropietario(@Param("propietarioId") Long propietarioId);

    @Query("""
            SELECT reserva
            FROM Reserva reserva
            LEFT JOIN FETCH reserva.servicio servicio
            LEFT JOIN FETCH reserva.lugar lugar
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
            LEFT JOIN FETCH reserva.servicio servicio
            LEFT JOIN FETCH servicio.creador servicioCreador
            LEFT JOIN FETCH reserva.lugar lugar
            LEFT JOIN FETCH lugar.propietario lugarPropietario
            JOIN FETCH reserva.turista turista
            WHERE reserva.id = :reservaId
            """)
    Optional<Reserva> buscarConPropietarioYTurista(@Param("reservaId") Long reservaId);
}
