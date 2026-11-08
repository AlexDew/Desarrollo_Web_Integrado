import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, UserRole } from '../models/auth.model';

const TOKEN_KEY = 'turismo.accessToken';
const ROLES_KEY = 'turismo.roles';
const EXPIRATION_KEY = 'turismo.expiresAt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenState = signal<string | null>(this.readToken());
  private readonly roleState = signal<UserRole[]>(this.readRoles());
  readonly roles = this.roleState.asReadonly();

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  login(credentials: { email: string; password: string }) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/api/auth/login`, credentials)
      .pipe(tap((response) => this.saveSession(response)));
  }

  register(data: { nombre: string; email: string; password: string }) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/api/auth/register`, data)
      .pipe(tap((response) => this.saveSession(response)));
  }

  addRole(rol: Exclude<UserRole, 'TURISTA'>) {
    return this.http
      .post<{ roles: UserRole[] }>(`${environment.apiUrl}/api/cuenta/roles`, { rol })
      .pipe(
        tap(({ roles }) => {
          this.roleState.set(roles);
          localStorage.setItem(ROLES_KEY, JSON.stringify(roles));
        }),
      );
  }

  hasAnyRole(roles: readonly string[]): boolean {
    return roles.some((role) => this.roleState().includes(role as UserRole));
  }

  getToken(): string | null {
    const token = this.tokenState();
    if (!token) return null;
    if (this.expiresAt() <= Date.now()) {
      this.logout();
      return null;
    }
    return token;
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLES_KEY);
    localStorage.removeItem(EXPIRATION_KEY);
    this.tokenState.set(null);
    this.roleState.set([]);
    this.expirationState.set(0);
  }

  private readonly expirationState = signal<number>(this.readExpiration());

  private expiresAt(): number {
    return this.expirationState();
  }

  private saveSession(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(ROLES_KEY, JSON.stringify(response.roles));
    const expiresAt = Date.now() + response.expiresInSeconds * 1000;
    localStorage.setItem(EXPIRATION_KEY, String(expiresAt));
    this.tokenState.set(response.token);
    this.roleState.set(response.roles);
    this.expirationState.set(expiresAt);
  }

  private readToken(): string | null {
    return typeof localStorage === 'undefined' ? null : localStorage.getItem(TOKEN_KEY);
  }

  private readRoles(): UserRole[] {
    if (typeof localStorage === 'undefined') return [];
    try {
      const roles: unknown = JSON.parse(localStorage.getItem(ROLES_KEY) ?? '[]');
      if (!Array.isArray(roles)) return [];
      return roles.filter(
        (role): role is UserRole =>
          role === 'TURISTA' || role === 'GUIA' || role === 'PROPIETARIO',
      );
    } catch {
      return [];
    }
  }

  private readExpiration(): number {
    if (typeof localStorage === 'undefined') return 0;
    const expiration = Number(localStorage.getItem(EXPIRATION_KEY));
    return Number.isFinite(expiration) ? expiration : 0;
  }
}
