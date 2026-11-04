# API de inventario

Los productos del inventario se representan como **insumos**, siguiendo el modelo del proyecto.
Este módulo implementa creación, consulta, edición, baja lógica y reactivación. Persiste en
PostgreSQL con las migraciones `V2__inventario.sql` y `V3__cantidades_enteras.sql`, conservando
las migraciones anteriores sin cambios. Desde `V4__establecimientos.sql` cada insumo pertenece a un
establecimiento, por eso las rutas quedan anidadas bajo `/api/establecimientos/{establecimientoId}`
(abreviado `{bar}` en las tablas). Las compras, los consumos por despacho y los ajustes de auditoría
están en [endpoints.md](endpoints.md).

| Método | Ruta | Resultado |
|---|---|---|
| POST | `{bar}/insumos` | Crea un insumo activo; devuelve 201, el producto y `Location`. |
| GET | `{bar}/insumos` | Lista los activos ordenados por nombre e ID; puede devolver `[]`. |
| GET | `{bar}/insumos?activo=false` | Lista los dados de baja. |
| GET | `{bar}/insumos/{id}` | Consulta un insumo, también si está inactivo. |
| GET | `{bar}/insumos/{id}/stock` | Consulta ID, unidad, stock actual y estado. |
| PUT | `{bar}/insumos/{id}` | Reemplaza nombre, costo, punto de reorden y contenido por envase de un insumo activo. |
| DELETE | `{bar}/insumos/{id}` | Da de baja sin eliminar el registro ni alterar stock; devuelve 204. |
| POST | `{bar}/insumos/{id}/alta` | Reactiva el mismo registro y devuelve 200 con el producto. |

Los IDs son UUID generados por el servidor. Baja y reactivación son idempotentes: repetirlas
conserva el estado y devuelve la misma respuesta HTTP. Un ID inexistente devuelve 404 en cualquier operación.
La edición de un insumo inactivo devuelve 409; primero debe reactivarse.

## Creación

```http
POST /api/establecimientos/{establecimientoId}/insumos
Content-Type: application/json

{
  "codigo": "GIN-01",
  "nombre": "Gin",
  "unidadMedida": "ML",
  "stockInicial": 750,
  "costoUnitario": 0.10,
  "puntoReorden": 100,
  "contenidoEnvase": 750
}
```

Todos los campos son obligatorios salvo `contenidoEnvase` (cantidad que trae cada envase comprado;
si falta vale 1). `codigo` admite hasta 50 caracteres y `nombre` hasta 150.
Se quitan espacios exteriores; el código se almacena en mayúsculas y es único dentro del
establecimiento, incluso entre los insumos inactivos. Crear `gin-01` después de `GIN-01` en el mismo
bar devuelve 409; otro bar puede usar el mismo código. Un establecimiento inexistente devuelve 404.

`unidadMedida` admite `ML` y `UNIDAD`. Stock inicial, stock actual y punto de reorden son
cantidades enteras en ambas unidades: se usa `BigDecimal` con escala 0 y hasta 12 dígitos.
La API rechaza cantidades como `0.5` o `750.0` con 400 y responde con números sin decimales.
El costo por unidad de medida conserva `BigDecimal` / `NUMERIC(12,2)` y admite hasta 2 decimales.
Todos deben ser mayores o iguales a cero. Las solicitudes que exceden la precisión se rechazan
sin redondeo silencioso. Los límites máximos son `999999999999` para cantidades y `9999999999.99`
para costos. La base usa `NUMERIC` con restricciones de integridad y rango para impedir
fracciones y valores negativos, incluso si se escribe directamente por SQL.

V3 elimina los ceros decimales de cantidades enteras existentes. Si una base con V2 contiene
fracciones, la migración se revierte sin alterar los datos; esas cantidades deben corregirse
explícitamente antes de volver a ejecutarla.

La respuesta incluye `id`, `codigo`, `nombre`, `unidadMedida`, `stockActual`, `costoUnitario`,
`puntoReorden`, `contenidoEnvase`, `costoEnvase`, `valorStock`, `activo` y `necesitaReposicion`.
Este último es verdadero cuando el stock es menor o igual al punto de reorden. Las alertas las emite
el Observer cuando un cambio de stock cruza ese punto (ver endpoints.md).

## Edición

```http
PUT /api/establecimientos/{establecimientoId}/insumos/{id}
Content-Type: application/json

{
  "nombre": "Gin premium",
  "costoUnitario": 0.25,
  "puntoReorden": 150
}
```

Nombre, costo y punto de reorden son obligatorios; `contenidoEnvase` es opcional y, si falta, se
conserva. El código, la unidad y el stock no se editan: el stock cambia solo con compras,
despachos y auditorías, que dejan un movimiento registrado. Las ediciones y los cambios de estado se serializan mediante
bloqueo de fila dentro de una transacción; la unicidad del código está protegida por PostgreSQL.

## Errores

Se usa `application/problem+json` (`ProblemDetail`): `type`, `title`, `status`, `detail` e `instance`.
Los errores de validación agregan un objeto `errores` con el nombre del campo y su mensaje.

| HTTP | Motivo |
|---|---|
| 400 | Campos inválidos/ausentes, cantidades negativas, precisión excesiva, unidad desconocida, JSON o UUID inválidos. |
| 404 | El insumo o el establecimiento no existen, o el insumo es de otro establecimiento. |
| 409 | Código duplicado, edición de inactivo, conflicto de integridad o concurrencia. |

## Verificación

Desde `backend/`, `./mvnw.cmd test` ejecuta pruebas unitarias de dominio, servicio y controlador.
`./mvnw.cmd verify` agrega integración con PostgreSQL 16 mediante Testcontainers y requiere Docker.
En Linux/macOS se usa `sh ./mvnw` en lugar de `./mvnw.cmd`.

La integración cubre el ciclo completo, cantidades enteras y costos con decimales, baja lógica,
reactivación, reserva del código de inactivos, validación de stock negativo por API y por SQL,
y dos creaciones concurrentes con el mismo código. También verifica el rechazo de fracciones
por API y SQL en ambas unidades, y la migración de bases con V2 sin pérdida de datos.
Usa bases de prueba aisladas.
