import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/services/services-list.component').then((module) => module.ServicesListComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((module) => module.LoginComponent),
  },
  {
    path: 'registro',
    loadComponent: () =>
      import('./features/auth/register.component').then((module) => module.RegisterComponent),
  },
  {
    path: 'cuenta/roles',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/auth/request-role.component').then((module) => module.RequestRoleComponent),
  },
  {
    path: 'lugares',
    loadComponent: () =>
      import('./features/places/places-list.component').then((module) => module.PlacesListComponent),
  },
  {
    path: 'gestion/lugares',
    canActivate: [authGuard, roleGuard],
    data: { management: true, roles: ['GUIA'] },
    loadComponent: () =>
      import('./features/places/places-list.component').then((module) => module.PlacesListComponent),
  },
  {
    path: 'gestion/lugares/nuevo',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['GUIA'] },
    loadComponent: () =>
      import('./features/places/place-form.component').then((module) => module.PlaceFormComponent),
  },
  {
    path: 'gestion/lugares/:id/editar',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['GUIA'] },
    loadComponent: () =>
      import('./features/places/place-form.component').then((module) => module.PlaceFormComponent),
  },
  {
    path: 'gestion/servicios',
    canActivate: [authGuard, roleGuard],
    data: { management: true, roles: ['PROPIETARIO'] },
    loadComponent: () =>
      import('./features/services/services-list.component').then((module) => module.ServicesListComponent),
  },
  {
    path: 'gestion/servicios/nuevo',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PROPIETARIO'] },
    loadComponent: () =>
      import('./features/services/service-form.component').then((module) => module.ServiceFormComponent),
  },
  {
    path: 'gestion/servicios/:id/editar',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PROPIETARIO'] },
    loadComponent: () =>
      import('./features/services/service-form.component').then((module) => module.ServiceFormComponent),
  },
  {
    path: 'reservas/nueva/lugar/:lugarId',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TURISTA'] },
    loadComponent: () =>
      import('./features/reservations/reservation-form.component').then(
        (module) => module.ReservationFormComponent,
      ),
  },
  {
    path: 'reservas/nueva/:servicioId',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TURISTA'] },
    loadComponent: () =>
      import('./features/reservations/reservation-form.component').then(
        (module) => module.ReservationFormComponent,
      ),
  },
  {
    path: 'reservas',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TURISTA'] },
    loadComponent: () =>
      import('./features/reservations/reservations-list.component').then(
        (module) => module.ReservationsListComponent,
      ),
  },
  {
    path: 'gestion/reservas',
    canActivate: [authGuard, roleGuard],
    data: { management: true, roles: ['GUIA', 'PROPIETARIO'] },
    loadComponent: () =>
      import('./features/reservations/reservations-list.component').then(
        (module) => module.ReservationsListComponent,
      ),
  },
  { path: '**', redirectTo: '' },
];
