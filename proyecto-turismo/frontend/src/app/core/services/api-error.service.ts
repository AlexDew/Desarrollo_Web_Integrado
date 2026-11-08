import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

export interface ParsedApiError {
  message: string;
  fields: Record<string, string>;
}

@Injectable({ providedIn: 'root' })
export class ApiErrorService {
  parse(error: unknown): ParsedApiError {
    if (!(error instanceof HttpErrorResponse)) {
      return { message: 'Ocurrió un error inesperado.', fields: {} };
    }

    const body = this.asRecord(error.error);
    const fields: Record<string, string> = {};
    const errors = body['errors'];
    const fieldErrors = body['fieldErrors'];

    if (errors && typeof errors === 'object' && !Array.isArray(errors)) {
      for (const [field, value] of Object.entries(errors)) {
        fields[field] = Array.isArray(value) ? String(value[0] ?? '') : String(value);
      }
    }

    if (Array.isArray(fieldErrors)) {
      for (const item of fieldErrors) {
        const fieldError = this.asRecord(item);
        const field = fieldError['field'];
        const message = fieldError['defaultMessage'] ?? fieldError['message'];
        if (typeof field === 'string' && typeof message === 'string') {
          fields[field] = message;
        }
      }
    }

    const message = [body['detail'], body['message'], body['title']]
      .find((value): value is string => typeof value === 'string' && value.length > 0);

    return {
      message: message ?? this.fallbackMessage(error.status),
      fields,
    };
  }

  private asRecord(value: unknown): Record<string, unknown> {
    return value !== null && typeof value === 'object'
      ? (value as Record<string, unknown>)
      : {};
  }

  private fallbackMessage(status: number): string {
    if (status === 401) return 'La sesión no es válida o ha expirado. Inicia sesión de nuevo.';
    if (status === 403) return 'Tu cuenta no tiene permiso para esta operación.';
    if (status === 404) return 'No se encontró el recurso solicitado.';
    if (status === 409) return 'La operación entra en conflicto con el estado actual.';
    return 'No se pudo completar la solicitud. Revisa los datos e inténtalo de nuevo.';
  }
}
