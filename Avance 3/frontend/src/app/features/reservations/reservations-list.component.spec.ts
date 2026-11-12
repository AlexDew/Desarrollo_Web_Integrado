import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { ReservationsListComponent } from './reservations-list.component';
import { environment } from '../../../environments/environment';

describe('ReservationsListComponent', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReservationsListComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { data: { management: true } } } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('loads received requests and refreshes the list after deciding', () => {
    const fixture = TestBed.createComponent(ReservationsListComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.loading()).toBe(true);
    const reservation = {
      id: 8,
      servicioId: 2,
      servicioNombre: 'Tour',
      turistaId: 3,
      turistaNombre: 'Visitante',
      turistaEmail: 'visitante@example.com',
      fecha: '2030-01-15',
      cantidadPersonas: 2,
      estado: 'PENDIENTE' as const,
    };
    http.expectOne(`${environment.apiUrl}/api/servicios/reservas`).flush([reservation]);
    fixture.detectChanges();

    expect(fixture.componentInstance.loading()).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('PENDIENTE');

    fixture.componentInstance.decide(8, 'CONFIRMADA');
    expect(fixture.componentInstance.actionInProgress()).toBe(true);
    http.expectOne(`${environment.apiUrl}/api/servicios/reservas/8/estado`).flush({
      ...reservation,
      estado: 'CONFIRMADA',
    });
    expect(fixture.componentInstance.actionInProgress()).toBe(false);
    expect(fixture.componentInstance.loading()).toBe(true);
    http.expectOne(`${environment.apiUrl}/api/servicios/reservas`).flush([]);
    fixture.detectChanges();

    expect(fixture.componentInstance.loading()).toBe(false);
    expect(fixture.componentInstance.reservations()).toEqual([]);
  });

  it('shows a server action error and clears the pending state', () => {
    const fixture = TestBed.createComponent(ReservationsListComponent);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/api/servicios/reservas`).flush([]);

    fixture.componentInstance.decide(8, 'RECHAZADA');
    http.expectOne(`${environment.apiUrl}/api/servicios/reservas/8/estado`).flush(
      { detail: 'No se pudo actualizar la solicitud.' },
      { status: 500, statusText: 'Internal Server Error' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.actionInProgress()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('No se pudo actualizar la solicitud.');
  });
});
