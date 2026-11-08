import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ApiErrorService } from './api-error.service';

describe('ApiErrorService', () => {
  let errors: ApiErrorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    errors = TestBed.inject(ApiErrorService);
  });

  it('extracts server field validation errors and the general message', () => {
    const parsed = errors.parse(new HttpErrorResponse({
      status: 400,
      error: {
        detail: 'Hay campos inválidos.',
        errors: { nombre: 'El nombre es obligatorio.' },
      },
    }));

    expect(parsed.message).toBe('Hay campos inválidos.');
    expect(parsed.fields['nombre']).toBe('El nombre es obligatorio.');
  });

  it('provides a useful message when a server error has no body', () => {
    const parsed = errors.parse(new HttpErrorResponse({ status: 401 }));

    expect(parsed.message).toContain('sesión');
  });
});
