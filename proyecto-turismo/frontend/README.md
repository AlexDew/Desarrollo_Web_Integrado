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

Inicia el backend con `CORS_ALLOWED_ORIGINS=http://localhost:4200`. Ese es el
origen permitido por defecto; en otros entornos configura la variable con los
orígenes explícitos, separados por comas. No uses `*` en producción.

## Funcionalidades

- Catálogo público de servicios y lugares.
- Registro, inicio de sesión y solicitud de rol de guía o propietario.
- CRUD de servicios propios para `GUIA` y `PROPIETARIO`.
- CRUD de lugares propios para `PROPIETARIO`.
- Solicitudes de reserva, cancelación del turista y respuesta del proveedor.
- Guards de sesión/rol, token Bearer, formularios reactivos y errores del backend.

## Verificación

```powershell
npm run build
npm test -- --watch=false
```
