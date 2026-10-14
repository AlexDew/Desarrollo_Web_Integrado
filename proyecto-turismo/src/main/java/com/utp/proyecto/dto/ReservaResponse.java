package com.utp.proyecto.dto;

import com.utp.proyecto.model.EstadoReserva;

import java.time.LocalDate;

public record ReservaResponse(
        Long id,
        Long servicioId,
        String servicioNombre,
        Long turistaId,
        String turistaNombre,
        String turistaEmail,
        LocalDate fecha,
        Integer cantidadPersonas,
        EstadoReserva estado) {
}
