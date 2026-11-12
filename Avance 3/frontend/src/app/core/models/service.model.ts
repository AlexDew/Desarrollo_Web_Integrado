export interface TouristService {
  id: number;
  nombre: string;
  tipo: string;
  descripcion: string;
  horarios: string;
  precio: number;
  ruta: string;
  contacto: string;
  aceptaReservas: boolean;
  atenciones?: number;
  calificacion?: number;
  activo?: boolean;
}

export type ServicePayload = Omit<
  TouristService,
  'id' | 'atenciones' | 'calificacion' | 'activo'
>;
