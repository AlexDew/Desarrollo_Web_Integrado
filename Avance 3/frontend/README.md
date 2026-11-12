# Frontend Turismo

Aplicación Angular standalone que consume la API Spring Boot del directorio padre.

## Requisitos

- Node.js compatible con Angular CLI 22.
- API Spring Boot disponible en `http://localhost:8080`.

## Desarrollo local

Desde esta carpeta instala dependencias y ejecuta el frontend:

```powershell
npm ci
npm start
```

La aplicación abre en `http://localhost:4200`. La URL base de la API está en
`src/environments/environment.ts`. Para usar otro servidor, cambia esa URL antes
de compilar.

El backend permite por defecto `http://localhost:4200` y
`http://127.0.0.1:4200`. Si abres el frontend desde otro origen, configura
`CORS_ALLOWED_ORIGINS` en el backend con el origen explícito; se permiten varios
separándolos por comas. No uses `*` en producción.

## Funcionalidades

- Catálogo público de servicios y lugares.
- Registro, inicio de sesión y solicitud de rol de guía o propietario de un servicio.
- CRUD de lugares propios para `GUIA`.
- CRUD de servicios propios y gestión de sus reservas para `PROPIETARIO`.
- Solicitudes de reserva para servicios y lugares, cancelación del turista y respuesta del proveedor.
- Guards de sesión/rol, token Bearer, formularios reactivos y errores del backend.

## Verificación

```powershell
npm run build
npm test -- --watch=false
```
