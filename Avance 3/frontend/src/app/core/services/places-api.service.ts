import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PlacePayload, TouristPlace } from '../models/place.model';

@Injectable({ providedIn: 'root' })
export class PlacesApiService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/lugares`;

  list(tipo?: string): Observable<TouristPlace[]> {
    const params = tipo ? new HttpParams().set('tipo', tipo) : undefined;
    return this.http.get<TouristPlace[]>(this.api, { params });
  }

  listOwned(): Observable<TouristPlace[]> {
    return this.http.get<TouristPlace[]>(`${environment.apiUrl}/api/servicios/lugares`);
  }

  get(id: number): Observable<TouristPlace> {
    return this.http.get<TouristPlace>(`${this.api}/${id}`);
  }

  create(payload: PlacePayload): Observable<TouristPlace> {
    return this.http.post<TouristPlace>(this.api, payload);
  }

  update(id: number, payload: PlacePayload): Observable<TouristPlace> {
    return this.http.put<TouristPlace>(`${this.api}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  rate(id: number, value: number): Observable<TouristPlace> {
    return this.http.patch<TouristPlace>(`${this.api}/${id}/calificacion`, null, {
      params: { valor: value },
    });
  }
}
