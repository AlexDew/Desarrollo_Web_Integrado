import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

describe('authInterceptor', () => {
  let http: HttpTestingController;
  const logout = vi.fn();

  beforeEach(() => {
    logout.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { getToken: () => 'signed.jwt.token', logout } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('sends the Bearer token to the configured API', () => {
    TestBed.inject(HttpClient).get(`${environment.apiUrl}/api/servicios/mios`).subscribe();

    const request = http.expectOne(`${environment.apiUrl}/api/servicios/mios`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer signed.jwt.token');
    request.flush([]);
  });

  it('does not send the token outside the configured API', () => {
    TestBed.inject(HttpClient).get('https://example.test/data').subscribe();

    const request = http.expectOne('https://example.test/data');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('logs out when the API rejects the token', () => {
    TestBed.inject(HttpClient)
      .get(`${environment.apiUrl}/api/servicios/mios`).subscribe({ error: () => undefined });

    http.expectOne(`${environment.apiUrl}/api/servicios/mios`).flush(
      { detail: 'Token inválido' },
      { status: 401, statusText: 'Unauthorized' },
    );
    expect(logout).toHaveBeenCalledOnce();
  });
});
