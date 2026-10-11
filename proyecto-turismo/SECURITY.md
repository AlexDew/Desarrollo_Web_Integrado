# Autenticación y autorización de la API

## Configuración local

Antes de iniciar la aplicación, configure `JWT_SECRET` como Base64 que represente
al menos 32 bytes aleatorios. En PowerShell:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:JWT_EXPIRATION_MS = "900000"
.\mvnw.cmd spring-boot:run
```

La clave no debe guardarse en el repositorio ni compartirse. Para entornos
compartidos o productivos, use un gestor de secretos.

## Registro e inicio de sesión

- `POST /api/auth/register`: acepta `nombre`, `email` y `password` (mínimo 8
  caracteres); crea una cuenta con rol `TURISTA`.
- `POST /api/auth/login`: acepta `email` y `password`.
- Ambas rutas devuelven un JWT. Envíelo en rutas protegidas como
  `Authorization: Bearer <token>`.

El registro público no permite elegir `PROPIETARIO` ni `GUIA`. Después de verificar
la cuenta, un administrador debe asignar el rol manualmente en SQL Server. Consulte
primero `usuarios.id` por correo y reemplace el rol en `usuario_roles`, por ejemplo
con `GUIA` o `PROPIETARIO`.

## Permisos de rutas

| Acceso | Operaciones |
| --- | --- |
| Público | Registro/inicio de sesión; consultas GET de `/api/lugares/**`, `/api/turista/lugares/**` y `/api/turista/servicios/**` |
| `TURISTA` | Registrar visitas/calificaciones y acceder al endpoint provisional de reservas |
| `PROPIETARIO` | Crear, editar y eliminar lugares |
| `PROPIETARIO` o `GUIA` | Crear, editar y eliminar servicios; consultar estadísticas y el listado administrativo de servicios |

La edición y eliminación de lugares y servicios solo se permite a quien creó el
registro. Los catálogos importados desde JSON no tienen propietario/creador y, por
tanto, no se pueden editar hasta que se les asigne uno. El endpoint actual de
reservas solo incrementa las atenciones del servicio; todavía no persiste una
reserva.
