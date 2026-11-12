export type ReservationStatus =
  | 'PENDIENTE'
  | 'CONFIRMADA'
  | 'RECHAZADA'
  | 'CANCELADA';

export interface Reservation {
  id: number;
  servicioId: number | null;
  servicioNombre: string | null;
  turistaId: number;
  turistaNombre: string;
  turistaEmail: string;
  fecha: string;
  cantidadPersonas: number;
  estado: ReservationStatus;
  lugarId?: number | null;
  lugarNombre?: string | null;
}

export interface ReservationRequest {
  fecha: string;
  cantidadPersonas: number;
}
