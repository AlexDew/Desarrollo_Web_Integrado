import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ServiceFormComponent } from './service-form.component';
import { environment } from '../../../environments/environment';

describe('ServiceFormComponent price validation', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ServiceFormComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('rejects prices outside decimal column precision without sending a request', () => {
    const fixture = TestBed.createComponent(ServiceFormComponent);
    const form = fixture.componentInstance.form;
    form.patchValue({
      nombre: 'Servicio',
      tipo: 'guia',
      descripcion: 'Descripción',
      horarios: 'Todos los días',
      precio: 100_000_000,
      ruta: 'Piura',
      contacto: '987654321',
      aceptaReservas: true,
    });

    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(form.controls.precio.hasError('max')).toBe(true);
    expect(fixture.nativeElement.textContent).toContain(
      'Ingrese datos válidos antes de publicar el servicio.',
    );
    expect(fixture.nativeElement.textContent).toContain('El valor supera el máximo permitido.');
    expect(fixture.componentInstance.submitting()).toBe(false);
    http.expectNone(`${environment.apiUrl}/api/servicios`);

    form.controls.precio.setValue(12.345);
    expect(form.controls.precio.hasError('pattern')).toBe(true);
  });

  it('shows backend save errors and exits the saving state', () => {
    const fixture = TestBed.createComponent(ServiceFormComponent);
    fixture.componentInstance.form.patchValue({
      nombre: 'Servicio',
      tipo: 'guia',
      descripcion: 'Descripción',
      horarios: 'Todos los días',
      precio: 45.5,
      ruta: 'Piura',
      contacto: '987654321',
      aceptaReservas: true,
    });

    fixture.componentInstance.submit();
    expect(fixture.componentInstance.submitting()).toBe(true);
    http.expectOne(`${environment.apiUrl}/api/servicios`).flush(
      { detail: 'No se pudo guardar el servicio.' },
      { status: 500, statusText: 'Internal Server Error' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.submitting()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('No se pudo guardar el servicio.');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(false);
  });

  it('requires a supported service type and a nine-digit contact', () => {
    const form = TestBed.createComponent(ServiceFormComponent).componentInstance.form;

    form.controls.tipo.setValue('otro');
    form.controls.contacto.setValue('12345');
    expect(form.controls.tipo.hasError('pattern')).toBe(true);
    expect(form.controls.contacto.hasError('pattern')).toBe(true);

    form.controls.tipo.setValue('restaurante');
    form.controls.contacto.setValue('987654321');
    expect(form.controls.tipo.valid).toBe(true);
    expect(form.controls.contacto.valid).toBe(true);
  });
});
