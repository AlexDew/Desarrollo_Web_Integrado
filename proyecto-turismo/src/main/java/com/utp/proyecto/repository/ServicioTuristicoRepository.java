package com.utp.proyecto.repository;

import com.utp.proyecto.model.ServicioTuristico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioTuristicoRepository extends JpaRepository<ServicioTuristico, Long> {
    @Query("SELECT servicio FROM ServicioTuristico servicio WHERE servicio.activo = true")
    List<ServicioTuristico> listarActivos();

    @Query("""
            SELECT servicio
            FROM ServicioTuristico servicio
            WHERE servicio.activo = true
              AND LOWER(servicio.tipo) = LOWER(:tipo)
            """)
    List<ServicioTuristico> listarActivosPorTipo(@Param("tipo") String tipo);

    @Query("""
            SELECT servicio
            FROM ServicioTuristico servicio
            WHERE servicio.activo = true
              AND servicio.aceptaReservas = true
            """)
    List<ServicioTuristico> listarActivosQueAceptanReservas();
}
