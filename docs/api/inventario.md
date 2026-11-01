# API de inventario

Los productos del inventario se representan como **insumos**, siguiendo el modelo del proyecto.
Este módulo implementa creación, consulta, edición, baja lógica y reactivación. Persiste en
PostgreSQL con las migraciones `V2__inventario.sql` y `V3__cantidades_enteras.sql`, conservando
las migraciones anteriores sin cambios.

| Método | Ruta | Resultado |
|---|---|---|
| POST | `/api/insumos` | Crea un insumo activo; devuelve 201, el producto y `Location`. |
| GET | `/api/insumos` | Lista los activos ordenados por nombre e ID; puede devolver `[]`. |
| GET | `/api/insumos?activo=false` | Lista los dados de baja. |
| GET | `/api/insumos/{id}` | Consulta un insumo, también si está inactivo. |
| GET | `/api/insumos/{id}/stock` | Consulta ID, unidad, stock actual y estado. |
| PUT | `/api/insumos/{id}` | Reemplaza nombre, costo y punto de reorden de un insumo activo. |
| DELETE | `/api/insumos/{id}` | Da de baja sin eliminar el registro ni alterar stock; devuelve 204. |
| POST | `/api/insumos/{id}/alta` | Reactiva el mismo registro y devuelve 200 con el producto. |

Los IDs son UUID generados por el servidor. Baja y reactivación son idempotentes: repetirlas
conserva el estado y devuelve la misma respuesta HTTP. Un ID inexistente devuelve 404 en cualquier operación.
La edición de un insumo inactivo devuelve 409; primero debe reactivarse.

## Creación

```http
POST /api/insumos
Content-Type: application/json

{
  "codigo": "GIN-01",
  "nombre": "Gin",
  "unidadMedida": "ML",
  "stockInicial": 750,
  "costoUnitario": 0.10,
  "puntoReorden": 100
}
```

Todos los campos son obligatorios. `codigo` admite hasta 50 caracteres y `nombre` hasta 150.
Se quitan espacios exteriores; el código se almacena en mayúsculas y es único incluso entre
los insumos inactivos. Crear `gin-01` después de `GIN-01` devuelve 409.

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
`puntoReorden`, `activo` y `necesitaReposicion`. Este último es verdadero cuando el stock es
menor o igual al punto de reorden; es un dato de consulta, sin envío de notificaciones.

## Edición

```http
PUT /api/insumos/{id}
Content-Type: application/json

{
  "nombre": "Gin premium",
  "costoUnitario": 0.25,
  "puntoReorden": 150
}
```

Los tres campos son obligatorios. El código, la unidad y el stock se conservan. El stock se
establece al crear el producto. Este alcance no agrega descuentos, compras, reposiciones,
recetas, comandas ni pantallas. Las ediciones y los cambios de estado se serializan mediante
bloqueo de fila dentro de una transacción; la unicidad del código está protegida por PostgreSQL.

## Errores

Se usa `application/problem+json` (`ProblemDetail`): `type`, `title`, `status`, `detail` e `instance`.
Los errores de validación agregan un objeto `errores` con el nombre del campo y su mensaje.

| HTTP | Motivo |
|---|---|
| 400 | Campos inválidos/ausentes, cantidades negativas, precisión excesiva, unidad desconocida, JSON o UUID inválidos. |
| 404 | El insumo no existe. |
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
