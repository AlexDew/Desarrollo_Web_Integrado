package com.utp.proyecto.repository;

import com.utp.proyecto.model.LugarTuristico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LugarTuristicoRepository extends JpaRepository<LugarTuristico, Long> {
}
