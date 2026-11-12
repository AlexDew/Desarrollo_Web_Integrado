package com.utp.proyecto.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CrearReservaRequest(
        @NotNull @Future LocalDate fecha,
        @NotNull @Min(1) Integer cantidadPersonas) {
}
