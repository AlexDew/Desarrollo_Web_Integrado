import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PlaceFormComponent } from './place-form.component';
import { environment } from '../../../environments/environment';

describe('PlaceFormComponent validation', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PlaceFormComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('rejects whitespace-only required fields and displays a general error', () => {
    const fixture = TestBed.createComponent(PlaceFormComponent);
    fixture.componentInstance.form.patchValue({
      nombre: '   ',
      tipo: 'playa',
      descripcion: 'Descripción',
      direccion: 'Piura',
      horarios: 'Todos los días',
      contacto: '987654321',
    });

    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance.form.controls.nombre.hasError('pattern')).toBe(true);
    expect(fixture.nativeElement.textContent).toContain(
      'Ingrese datos válidos antes de guardar el lugar.',
    );
    http.expectNone(`${environment.apiUrl}/api/lugares`);
  });

  it('accepts only Playa or Cultural and a nine-digit contact', () => {
    const fixture = TestBed.createComponent(PlaceFormComponent);
    const { tipo, contacto } = fixture.componentInstance.form.controls;

    tipo.setValue('naturaleza');
    contacto.setValue('12345678');
    expect(tipo.hasError('pattern')).toBe(true);
    expect(contacto.hasError('pattern')).toBe(true);

    tipo.setValue('cultural');
    contacto.setValue('987654321');
    expect(tipo.valid).toBe(true);
    expect(contacto.valid).toBe(true);
  });

  it('shows a server error and restores the place save button', () => {
    const fixture = TestBed.createComponent(PlaceFormComponent);
    fixture.componentInstance.form.patchValue({
      nombre: 'Lugar',
      tipo: 'cultural',
      descripcion: 'Descripción',
      direccion: 'Piura',
      horarios: 'Todos los días',
      contacto: '987654321',
    });

    fixture.componentInstance.submit();
    expect(fixture.componentInstance.submitting()).toBe(true);
    http.expectOne(`${environment.apiUrl}/api/lugares`).flush(
      { detail: 'No se pudo guardar el lugar.' },
      { status: 500, statusText: 'Internal Server Error' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.submitting()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('No se pudo guardar el lugar.');
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(false);
  });
});
