import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();
  if (!token || !request.url.startsWith(environment.apiUrl)) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }),
  ).pipe(
    catchError((error: unknown) => {
      if (
        error !== null &&
        typeof error === 'object' &&
        'status' in error &&
        error.status === 401
      ) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};
