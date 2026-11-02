# ADR-002 — Cuatro roles, varios establecimientos y reglas aplicadas en la aplicación

**Estado:** PROPUESTA implementada en la rama `feature/app-roles-patrones`. Queda pendiente de revisión del equipo
(no se integra a `main` sin la PR aprobada).

## Contexto

El README define dos roles (Bartender y Encargado) y un solo inventario. El prototipo HTML ya separaba los datos
por establecimiento (Bar Ámbar y La Esquina). Para la entrega se pidió una vista por cada usuario: Cliente,
Bartender, Encargado y Dueño.

## Decisiones

1. **Cuatro roles de interfaz, sin autenticación.** Se elige rol, nombre y bar al ingresar; cada pestaña guarda su
   sesión en `sessionStorage`. `VistaPorRol` redirige a la vista inicial si el rol no tiene permitida la ruta.
   - Cliente: carta con precio vigente, pedido (comanda con origen `CLIENTE`) y seguimiento en vivo.
   - Bartender: comandas, stock de solo lectura y recuento físico de cierre de turno.
   - Encargado: insumos, compras, auditoría y alertas.
   - Dueño: resumen de todos sus bares, reportes, política de precios, menú y establecimientos.
2. **Varios establecimientos.** Nueva tabla `establecimiento` (V4). Insumos, recetas, comandas, movimientos,
   auditorías y alertas llevan `establecimiento_id`, y las rutas quedan bajo `/api/establecimientos/{id}`.
   El código de insumo pasa a ser único por establecimiento. Los insumos que existieran antes de V4 se asignan a
   "Bar Ámbar".
3. **Reglas del ADR-001 que se implementaron tal como estaban propuestas:**
   descuento al despachar en una sola transacción; anular desde `Registrada`, `EnPreparacion` y `Despachada`
   con motivo obligatorio; la devolución no repone stock y deja de contar como ingreso; precio fijado al registrar
   y guardado por ítem; alertas después del commit; un fallo de notificación no deshace el descuento.
4. **Concurrencia.** Bloqueo `PESSIMISTIC_WRITE` de la comanda y de cada insumo en orden de id (evita deadlocks y
   doble descuento). La fila del establecimiento se bloquea para numerar comandas y auditorías.
5. **Disponibilidad al registrar.** Registrar un pedido verifica que hoy alcance el stock, sin reservarlo. Si dos
   pedidos compiten, el segundo despacho falla con 409 y no descuenta nada.
6. **Margen mínimo de Happy Hour.** Se expresa sobre el costo: el precio nunca baja de `costo × (1 + margen)`.
   Por defecto 50 %.
7. **Datos de demostración.** Migración repetible `db/demo/R__datos_demo.sql`, activa con el perfil `demo`
   (por defecto en Docker Compose). Es idempotente y no pisa cambios hechos desde la aplicación.
8. **Traza de patrones.** `TrazaPatrones` guarda en memoria los últimos eventos de cada patrón para mostrarlos en
   la defensa. Dentro de una transacción solo publica si se confirma.

## Consecuencias

- La API anterior `/api/insumos` deja de existir; las pruebas y `docs/api/inventario.md` se actualizaron.
- Cualquiera puede elegir cualquier rol: la separación es de interfaz. El inicio de sesión real queda como trabajo futuro.
- Email, SMS y WhatsApp están simulados: se registran en la tabla `notificacion` y en el log.
- La traza se pierde al reiniciar el backend; es una ayuda para la demostración, no un registro de auditoría.
