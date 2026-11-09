import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { convertToParamMap } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { ReservationFormComponent } from './reservation-form.component';
import { environment } from '../../../environments/environment';

describe('ReservationFormComponent validation', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReservationFormComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ servicioId: '5' }) } },
        },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('rejects past dates and non-integer person counts', () => {
    const fixture = TestBed.createComponent(ReservationFormComponent);
    const { fecha, cantidadPersonas } = fixture.componentInstance.form.controls;

    fecha.setValue('2000-01-01');
    cantidadPersonas.setValue(1.5);
    expect(fecha.hasError('minDate')).toBe(true);
    expect(cantidadPersonas.hasError('pattern')).toBe(true);

    fixture.componentInstance.submit();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent)
      .toContain('Ingrese datos válidos antes de enviar la solicitud.');
    http.expectNone(`${environment.apiUrl}/api/turista/servicios/5/reservas`);
  });

  it('shows a server error and restores the reservation submit button', () => {
    const fixture = TestBed.createComponent(ReservationFormComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      fecha: fixture.componentInstance.minimumDate,
      cantidadPersonas: 2,
    });

    expect(fixture.componentInstance.form.valid).toBe(true);
    fixture.componentInstance.submit();
    expect(fixture.componentInstance.submitting()).toBe(true);
    http.expectOne(`${environment.apiUrl}/api/turista/servicios/5/reservas`).flush(
      { detail: 'El servicio ya no acepta reservas.' },
      { status: 409, statusText: 'Conflict' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.submitting()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('El servicio ya no acepta reservas.');
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(false);
  });
});
