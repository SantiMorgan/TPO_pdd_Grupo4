# ADR-001 — Reglas de despacho, anulación, concurrencia, precios y números

**Estado:** PROPUESTA. Nada de esto es regla de negocio hasta que el equipo (y, donde se indica, el docente) la apruebe.
Cada punto marca si **sale del UML** o si es una **propuesta nueva**.

## 1. Comandos y estados

| Comando | Estado actual | Resultado | Efecto sobre stock | Si el estado no corresponde |
|---|---|---|---|---|
| `registrar` | — | `Registrada` | Ninguno | — |
| `preparar` | `Registrada` | `EnPreparacion` | Ninguno | `TransicionInvalida` → 409 |
| `despachar` | `EnPreparacion` | `Despachada` | **Descuento atómico** (sección 2) | `TransicionInvalida` → 409 |
| `anular(motivo)` | `Registrada` *(propuesta nueva: el UML no la dibuja)* | `Anulada` | Ninguno | `TransicionInvalida` → 409 |
| `anular(motivo)` | `EnPreparacion` (UML: «fallido») | `Anulada` | Ninguno (todavía no se descontó) | `TransicionInvalida` → 409 |
| `anular(motivo)` | `Despachada` (UML: «devuelto») | `Anulada` | **No repone stock** (el trago ya se elaboró); el importe deja de contar como ingreso | `TransicionInvalida` → 409 |
| cualquiera | `Anulada` | — | — | `TransicionInvalida` → 409 |

- El motivo de anulación es obligatorio.
- **Pendiente de aprobación (decisión 3):** si el grupo prefiere reponer stock al anular después de despachar, no cambia solo el
  estado `Anulada`: cambian también el inventario, el registro de movimientos y el cálculo de ingresos. Hay que decidirlo antes de codificar `C7`.
- **Limitación a declarar:** el insumo perdido en una preparación fallida no se modela como merma.

## 2. Contrato de `despacharComanda(comandaId)`

**Entrada:** `comandaId: String`. **Salida:** resumen de la comanda (`id`, `estado`, `total`). **Todo ocurre en una única transacción:**

1. Bloquear la comanda (`PESSIMISTIC_WRITE`) y verificar que está en `EnPreparacion`.
2. Obtener los `Consumo` de cada ítem (`Receta.consumos(...)`) y **sumarlos por `insumoId`** (dos tragos con el mismo licor se validan contra el stock total).
3. `Inventario.descontarTodo(consumos)`: valida unidades y aplica los descuentos en orden de `insumoId`.
4. Pasar la comanda a `Despachada`.
5. Commit. **Recién entonces** se escribe el log de éxito y se disparan las alertas de stock bajo.

| Situación | Excepción de dominio | HTTP | Efectos |
|---|---|---|---|
| Comanda inexistente | `ComandaNoEncontradaException` | 404 | Ninguno |
| Estado incorrecto o comanda ya despachada | `TransicionInvalidaException` | 409 | **Ninguno** (no hay segundo descuento) |
| Falta stock de uno o más insumos | `StockInsuficienteException` (lista los faltantes) | 409 | Ninguno: rollback total |
| Unidad incompatible | `UnidadIncompatibleException` | 422 | Ninguno |
| Falla el envío de una alerta | — | 200 | La operación ya está confirmada; WARN con la causa |

## 3. Concurrencia

- **Doble despacho simultáneo de la misma comanda:** el bloqueo de la fila hace que el segundo espere y, al entrar, reciba 409.
- **Dos comandas distintas sobre el mismo insumo:** el descuento se hace con una actualización condicional,
  `UPDATE insumo SET stock_actual = stock_actual - :c WHERE id = :id AND stock_actual >= :c`.
  Si afecta 0 filas, falta stock y se revierte la transacción completa. Se procesa **en orden de `insumoId`** para evitar deadlocks.
- Es una **propuesta nueva**; el UML no define concurrencia.

## 4. Precio

- La estrategia de precio vive en `Comanda` (UML general). La clase `Venta` del diagrama de Strategy se elimina: `Comanda.calcularTotal()` suma `item.calcularPrecio(estrategia)`.
- **Propuesta:** el precio se fija **al registrar** la comanda con la estrategia vigente en ese momento, y se guardan `precio_unitario` y el nombre de la estrategia aplicada en cada ítem. Los importes históricos no se recalculan.
- No hay tabla de ventas aparte: los ingresos salen de las comandas `Despachada` (las `Anulada` no cuentan).
- Cortesía (`esCortesia`): descuenta stock y el total es 0 *(deseable, C8)*.

## 5. Números y unidades

| Dato | Tipo Java | Columna | Regla |
|---|---|---|---|
| Cantidades | `BigDecimal` | `NUMERIC(12,3)` | Siempre con su `UnidadMedida` (`ML`, `UNIDAD`) |
| Montos | `BigDecimal` | `NUMERIC(12,2)` | Redondeo `HALF_UP`, **una sola vez por ítem**, nunca en cálculos intermedios |

- `Consumo(insumoId, cantidad, unidad)` representa lo que consume **un ítem** (un trago). Lo define el Hito 0, no un frente.
- `descontarTodo` rechaza el consumo si la unidad no coincide con la del insumo. Cantidades negativas o cero se rechazan siempre.

## 6. Alertas y logging

- El Observer se mantiene como en el UML. Las alertas se ejecutan **después del commit** (propuesta: evento transaccional `AFTER_COMMIT` de Spring), para no avisar de un descuento que luego se revirtió.
- Un fallo de notificación nunca deshace el descuento. Se registra como WARN con la causa.
- Se permite un log de negocio por insumo descontado; el de éxito se escribe solo tras el commit.

## 7. Preguntas que el equipo debe responder (o llevar al docente)

1. ¿Se aprueba que anular una comanda despachada **no** repone stock? (sección 1)
2. ¿Se aprueba fijar el precio al registrar, y no al despachar? (sección 4)
3. ¿Se aprueba permitir `Registrada → Anulada`? (sección 1)
4. ¿La tolerancia de auditoría sale de `Receta.tolerancia` o de un umbral por insumo? (solo bloquea el frente deseable de auditoría)
