package com.utp.proyecto.dto;

import com.utp.proyecto.model.Rol;
import jakarta.validation.constraints.NotNull;

public record SolicitudRolRequest(@NotNull Rol rol) {
}
