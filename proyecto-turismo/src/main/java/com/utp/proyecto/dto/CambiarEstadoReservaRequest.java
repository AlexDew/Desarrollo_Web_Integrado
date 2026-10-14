package com.utp.proyecto.dto;

import com.utp.proyecto.model.EstadoReserva;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoReservaRequest(@NotNull EstadoReserva estado) {
}
