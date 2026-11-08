import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  if (!auth.isAuthenticated()) {
    return inject(Router).createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    });
  }

  const roles = route.data['roles'] as string[] | undefined;
  if (roles?.length && !auth.hasAnyRole(roles)) {
    return inject(Router).createUrlTree(['/']);
  }
  return true;
};
