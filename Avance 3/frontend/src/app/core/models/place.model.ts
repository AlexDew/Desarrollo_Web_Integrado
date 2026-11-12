export interface TouristPlace {
  id: number;
  nombre: string;
  tipo: string;
  descripcion: string;
  direccion: string;
  horarios: string;
  contacto: string;
  visitantes?: number;
  calificacion?: number;
  activo?: boolean;
}

export type PlacePayload = Omit<
  TouristPlace,
  'id' | 'visitantes' | 'calificacion' | 'activo'
>;
