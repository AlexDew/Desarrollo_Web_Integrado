import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NavbarComponent } from './navbar.component';

describe('NavbarComponent roles', () => {
  afterEach(() => {
    localStorage.removeItem('turismo.accessToken');
    localStorage.removeItem('turismo.roles');
    localStorage.removeItem('turismo.expiresAt');
  });

  it('shows current roles in the navbar using readable labels', () => {
    localStorage.setItem('turismo.accessToken', 'signed.jwt.token');
    localStorage.setItem('turismo.roles', JSON.stringify(['TURISTA', 'GUIA', 'PROPIETARIO']));
    localStorage.setItem('turismo.expiresAt', String(Date.now() + 60_000));
    TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [provideHttpClient(), provideRouter([])],
    });

    const fixture = TestBed.createComponent(NavbarComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Turista');
    expect(fixture.nativeElement.textContent).toContain('Guía');
    expect(fixture.nativeElement.textContent).toContain('Propietario de un servicio');
  });
});
