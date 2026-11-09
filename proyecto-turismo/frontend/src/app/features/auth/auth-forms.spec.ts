import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { RegisterComponent } from './register.component';
import { RequestRoleComponent } from './request-role.component';
import { environment } from '../../../environments/environment';

describe('Authentication form validation', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoginComponent, RegisterComponent, RequestRoleComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('requires a valid email and a password to log in', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const { email, password } = fixture.componentInstance.form.controls;

    expect(email.invalid).toBe(true);
    expect(password.invalid).toBe(true);

    email.setValue('not-an-email');
    password.setValue('password');
    expect(email.hasError('email')).toBe(true);
    expect(password.valid).toBe(true);
  });

  it('rejects whitespace-only names and passwords outside backend limits', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    const { nombre, email, password } = fixture.componentInstance.form.controls;

    nombre.setValue('   ');
    email.setValue('turista@example.com');
    password.setValue('1234567');
    expect(nombre.hasError('pattern')).toBe(true);
    expect(password.hasError('minlength')).toBe(true);

    nombre.setValue('Turista');
    password.setValue('x'.repeat(73));
    expect(nombre.valid).toBe(true);
    expect(password.hasError('maxlength')).toBe(true);
  });

  it('shows a general message when registration is submitted with invalid data', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('Ingrese datos válidos');
    expect(fixture.componentInstance.form.touched).toBe(true);
    http.expectNone(`${environment.apiUrl}/api/auth/register`);
  });

  it('displays the server error and restores the login button after rejection', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.componentInstance.form.setValue({
      email: 'turista@example.com',
      password: 'wrong-password',
    });

    fixture.componentInstance.submit();
    http.expectOne(`${environment.apiUrl}/api/auth/login`).flush(
      { detail: 'Correo o contraseña incorrectos' },
      { status: 401, statusText: 'Unauthorized' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('Correo o contraseña incorrectos');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(false);
  });

  it('displays a duplicate-account error returned by registration', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    fixture.componentInstance.form.setValue({
      nombre: 'Turista',
      email: 'turista@example.com',
      password: 'Password123!',
    });

    fixture.componentInstance.submit();
    http.expectOne(`${environment.apiUrl}/api/auth/register`).flush(
      { detail: 'Ya existe una cuenta con ese correo' },
      { status: 409, statusText: 'Conflict' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('Ya existe una cuenta con ese correo');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(false);
  });

  it('finishes the role request and shows a success message', () => {
    const fixture = TestBed.createComponent(RequestRoleComponent);
    fixture.componentInstance.form.controls.rol.setValue('GUIA');

    fixture.componentInstance.submit();
    expect(fixture.componentInstance.submitting()).toBe(true);
    http.expectOne(`${environment.apiUrl}/api/cuenta/roles`).flush({
      roles: ['TURISTA', 'GUIA'],
    });
    fixture.detectChanges();

    expect(fixture.componentInstance.submitting()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="status"]').textContent)
      .toContain('El rol se añadió a tu cuenta.');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(false);
  });

  it('shows a role request error and stops the loading state', () => {
    const fixture = TestBed.createComponent(RequestRoleComponent);
    fixture.componentInstance.submit();

    http.expectOne(`${environment.apiUrl}/api/cuenta/roles`).flush(
      { detail: 'La cuenta no está autorizada.' },
      { status: 403, statusText: 'Forbidden' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.submitting()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent)
      .toContain('La cuenta no está autorizada.');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(false);
  });
});
