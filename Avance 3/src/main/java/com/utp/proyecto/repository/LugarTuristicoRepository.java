package com.utp.proyecto.repository;

import com.utp.proyecto.model.LugarTuristico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LugarTuristicoRepository extends JpaRepository<LugarTuristico, Long> {
    List<LugarTuristico> findByPropietario_IdAndActivoTrue(Long propietarioId);

    @Query("""
            SELECT lugar.id AS id, lugar.contacto AS contacto
            FROM LugarTuristico lugar
            """)
    List<ContactoRegistrado> listarContactosRegistrados();

    @Query("""
            SELECT lugar
            FROM LugarTuristico lugar
            WHERE lugar.activo = true
              AND LOWER(lugar.tipo) = LOWER(:tipo)
            """)
    List<LugarTuristico> listarActivosPorTipo(@Param("tipo") String tipo);

    @Query("""
            SELECT lugar
            FROM LugarTuristico lugar
            WHERE lugar.activo = true
            ORDER BY lugar.calificacion DESC
            """)
    List<LugarTuristico> listarActivosMejorCalificados();

    @Query("""
            SELECT lugar
            FROM LugarTuristico lugar
            WHERE lugar.activo = true
            ORDER BY lugar.visitantes DESC
            """)
    List<LugarTuristico> listarActivosMasVisitados();
}
