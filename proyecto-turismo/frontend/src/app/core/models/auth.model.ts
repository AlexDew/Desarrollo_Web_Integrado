export type UserRole = 'TURISTA' | 'GUIA' | 'PROPIETARIO';

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  email: string;
  roles: UserRole[];
}
