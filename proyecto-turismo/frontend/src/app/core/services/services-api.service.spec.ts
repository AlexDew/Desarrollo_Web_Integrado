import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from '../interceptors/auth.interceptor';
import { AuthService } from './auth.service';
import { ServicesApiService } from './services-api.service';

describe('ServicesApiService', () => {
  let api: ServicesApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ServicesApiService,
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: { getToken: () => 'signed.jwt.token' },
        },
      ],
    });
    api = TestBed.inject(ServicesApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the public catalog through its public API route', () => {
    api.listPublic().subscribe((services) => expect(services).toEqual([]));

    const request = http.expectOne('http://localhost:8080/api/turista/servicios');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.get('Authorization')).toBe('Bearer signed.jwt.token');
    request.flush([]);
  });

  it('attaches the JWT to protected service operations', () => {
    api.listManaged().subscribe((services) => expect(services.length).toBe(0));

    const request = http.expectOne('http://localhost:8080/api/servicios/mios');
    expect(request.request.headers.get('Authorization')).toBe('Bearer signed.jwt.token');
    request.flush([]);
  });
});
