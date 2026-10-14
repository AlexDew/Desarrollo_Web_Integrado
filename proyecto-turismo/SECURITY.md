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

El registro público asigna solo el rol `TURISTA`. Una persona autenticada puede
agregar a su propia cuenta el rol `GUIA` o `PROPIETARIO`:

```http
POST /api/cuenta/roles
Authorization: Bearer <token>
Content-Type: application/json

{"rol":"GUIA"}
```

El endpoint responde con la lista de roles de la cuenta. La asignación es inmediata,
conserva `TURISTA` y es idempotente si ya tiene el rol. No se aceptan `TURISTA` ni
roles desconocidos en esta operación. El filtro JWT vuelve a cargar el usuario y sus
roles desde la base de datos en cada petición, por lo que el mismo token puede usar
los permisos nuevos inmediatamente. Esta implementación no incluye verificación ni
aprobación administrativa de quienes solicitan `GUIA` o `PROPIETARIO`.

## Permisos de rutas

| Acceso | Operaciones |
| --- | --- |
| Público | Registro/inicio de sesión; consultas GET de `/api/lugares/**`, `/api/turista/lugares/**` y `/api/turista/servicios/**` |
| `TURISTA` | Registrar visitas/calificaciones, solicitar reservas, consultar las propias y cancelar solicitudes pendientes |
| `PROPIETARIO` | Crear, editar y eliminar lugares |
| `PROPIETARIO` o `GUIA` | Crear, editar y eliminar servicios; consultar estadísticas, reservas de sus servicios y decidir solicitudes |

La edición y eliminación de lugares y servicios solo se permite a quien creó el
registro. Los catálogos importados desde JSON no tienen propietario/creador y, por
tanto, no se pueden editar hasta que se les asigne uno ni recibir reservas hasta
que tengan un creador responsable.

## Solicitudes de reserva

Las reservas son solicitudes, no compras: no se procesa ningún pago en la API.
El servicio debe estar activo y aceptar reservas. El turista autenticado envía
una fecha futura y la cantidad de personas:

```http
POST /api/turista/servicios/{servicioId}/reservas
Authorization: Bearer <token>
Content-Type: application/json

{"fecha":"2026-12-20","cantidadPersonas":2}
```

La solicitud se crea en estado `PENDIENTE`. El turista puede consultar sus
solicitudes en `GET /api/turista/reservas` y cancelar una pendiente con
`DELETE /api/turista/reservas/{reservaId}`. El propietario o guía responsable
consulta las solicitudes de sus propios servicios con `GET /api/servicios/reservas`
y actualiza una pendiente mediante
`PATCH /api/servicios/reservas/{reservaId}/estado`, enviando
`{"estado":"CONFIRMADA"}` o `{"estado":"RECHAZADA"}`. Las solicitudes no alteran
el contador de atenciones; las transiciones desde un estado final se rechazan.
No se permite eliminar un servicio que ya tenga solicitudes, para conservar su
historial y evitar perder su relación con las reservas.
