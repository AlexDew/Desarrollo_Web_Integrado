package com.utp.proyecto.repository;

import com.utp.proyecto.model.ServicioTuristico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioTuristicoRepository extends JpaRepository<ServicioTuristico, Long> {
}
