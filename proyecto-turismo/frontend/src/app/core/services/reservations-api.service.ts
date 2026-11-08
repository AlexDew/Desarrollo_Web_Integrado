import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Reservation, ReservationRequest } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationsApiService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api`;

  create(serviceId: number, request: ReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(
      `${this.api}/turista/servicios/${serviceId}/reservas`,
      request,
    );
  }

  listMine(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.api}/turista/reservas`);
  }

  cancel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/turista/reservas/${id}`);
  }

  listForManagedServices(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.api}/servicios/reservas`);
  }

  changeStatus(id: number, estado: 'CONFIRMADA' | 'RECHAZADA'): Observable<Reservation> {
    return this.http.patch<Reservation>(`${this.api}/servicios/reservas/${id}/estado`, {
      estado,
    });
  }
}
