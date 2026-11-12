import { routes } from './app.routes';

describe('role-based management routes', () => {
  it('assigns place management only to GUIA', () => {
    for (const path of ['gestion/lugares', 'gestion/lugares/nuevo', 'gestion/lugares/:id/editar']) {
      const route = routes.find((candidate) => candidate.path === path);
      expect(route?.data?.['roles']).toEqual(['GUIA']);
    }
  });

  it('assigns service management only to PROPIETARIO', () => {
    for (const path of [
      'gestion/servicios',
      'gestion/servicios/nuevo',
      'gestion/servicios/:id/editar',
    ]) {
      const route = routes.find((candidate) => candidate.path === path);
      expect(route?.data?.['roles']).toEqual(['PROPIETARIO']);
    }
  });

  it('allows both provider roles to manage their own reservations', () => {
    const route = routes.find((candidate) => candidate.path === 'gestion/reservas');
    expect(route?.data?.['roles']).toEqual(['GUIA', 'PROPIETARIO']);
  });
});
