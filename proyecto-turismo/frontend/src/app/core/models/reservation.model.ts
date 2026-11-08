export type ReservationStatus =
  | 'PENDIENTE'
  | 'CONFIRMADA'
  | 'RECHAZADA'
  | 'CANCELADA';

export interface Reservation {
  id: number;
  servicioId: number;
  servicioNombre: string;
  turistaId: number;
  turistaNombre: string;
  turistaEmail: string;
  fecha: string;
  cantidadPersonas: number;
  estado: ReservationStatus;
}

export interface ReservationRequest {
  fecha: string;
  cantidadPersonas: number;
}
