import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ServicePayload, TouristService } from '../models/service.model';

@Injectable({ providedIn: 'root' })
export class ServicesApiService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api`;

  listPublic(): Observable<TouristService[]> {
    return this.http.get<TouristService[]>(`${this.api}/turista/servicios`);
  }

  listManaged(): Observable<TouristService[]> {
    return this.http.get<TouristService[]>(`${this.api}/servicios/mios`);
  }

  create(payload: ServicePayload): Observable<TouristService> {
    return this.http.post<TouristService>(`${this.api}/servicios`, payload);
  }

  update(id: number, payload: ServicePayload): Observable<TouristService> {
    return this.http.put<TouristService>(`${this.api}/servicios/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/servicios/${id}`);
  }
}
