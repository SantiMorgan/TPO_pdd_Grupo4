# API de BarStock Solutions

Todas las rutas de negocio cuelgan de un establecimiento: `{bar}` = `/api/establecimientos/{establecimientoId}`.
Los controladores solo validan formato y delegan en `BarStockFacade`. Los errores usan `ProblemDetail`
(`application/problem+json`). El CRUD de insumos está detallado en [inventario.md](inventario.md).

| HTTP | Significado |
|---|---|
| 400 | JSON, tipos, UUID o validaciones de campos inválidos (`errores` por campo). |
| 404 | El establecimiento o el recurso no existen, o el recurso es de otro establecimiento. |
| 409 | Regla de negocio: transición de estado inválida, stock insuficiente (`faltantes`), duplicados, concurrencia. |
| 422 | Unidad de medida incompatible entre el consumo y el insumo. |

## Establecimientos

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/establecimientos` | Lista los bares con su política de precios y sus canales de alerta. |
| GET | `/api/establecimientos/{id}` | Detalle. |
| POST | `/api/establecimientos` | `{codigo, nombre, zona, direccion, descripcion, horario}` → 201. `direccion` es obligatoria; sin `horario` se usa 12–16 y 20–2. |
| PUT | `/api/establecimientos/{id}` | Edita nombre, zona, dirección, descripción y, si viene, el horario. |
| PUT | `{bar}/politica-precio` | Strategy. `{modo, inicioHappyHour, finHappyHour, descuentoHappyHour, margenMinimo, horaSimulada}`. `modo`: `AUTOMATICO`, `NORMAL`, `HAPPY_HOUR`. |
| PUT | `{bar}/alertas` | Simple Factory. `{canales: ["PANEL","EMAIL","SMS","WHATSAPP"], email, telefono}`. |

`horario` = `{mediodiaApertura, mediodiaCierre, nocheApertura, nocheCierre}` (`"HH:mm"`). Tiene que seguir el orden de
la jornada (de 6:00 a 5:59): la noche puede cerrar después de medianoche, hasta las 5:59.

## Carta y precios (Strategy)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `{bar}/carta` | Recetas del menú con el precio de la estrategia vigente, el costo y si hoy hay stock para una unidad. Incluye `estrategia: {codigo, nombre, motivo, hora, horaSimulada}`. |
| GET | `{bar}/precios/comparacion` | Cada receta calculada con las dos estrategias (normal y Happy Hour), en el orden de `estrategias`. |

En modo `AUTOMATICO` rige Happy Hour dentro de su horario (puede cruzar la medianoche); fuera de él, precio
normal. Happy Hour nunca baja de `costo × (1 + margenMinimo/100)` ni sube del precio base.

## Recetas

| Método | Ruta | Descripción |
|---|---|---|
| GET | `{bar}/recetas?incluirFueraDelMenu=false` | Recetas con costo teórico, disponibilidad e ingredientes. |
| GET | `{bar}/recetas/{id}` | Detalle. |
| POST | `{bar}/recetas` | `{nombre, descripcion, icono, precioBase, ingredientes: [{insumoId, cantidad}]}` → 201. |
| PUT | `{bar}/recetas/{id}` | Reemplaza datos e ingredientes. |
| DELETE | `{bar}/recetas/{id}` | Baja lógica: sale del menú; las comandas anteriores la siguen referenciando. |
| POST | `{bar}/recetas/{id}/alta` | Vuelve al menú. |

## Comandas (State + Facade)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `{bar}/comandas` | `{origen: "CLIENTE"\|"BARTENDER", cliente, items: [{recetaId, cantidad}]}` → 201 en `REGISTRADA`. Verifica que hoy alcance el stock (sin reservarlo) y fija el precio con la estrategia vigente. |
| GET | `{bar}/comandas?enCurso=true` | Registradas y en preparación, de la más antigua a la más nueva. |
| GET | `{bar}/comandas?limite=30` | Las últimas comandas. |
| GET | `{bar}/comandas/{id}` | Detalle (lo consulta el cliente para seguir su pedido). |
| POST | `{bar}/comandas/{id}/preparar` | `REGISTRADA → EN_PREPARACION`. |
| POST | `{bar}/comandas/{id}/despachar` | `EN_PREPARACION → DESPACHADA` y descuento atómico de todos los insumos. |
| POST | `{bar}/comandas/{id}/anular` | `{motivo}` (5 a 220 caracteres). Solo desde `REGISTRADA` o `EN_PREPARACION`. |
| POST | `{bar}/comandas/{id}/devoluciones` | `{itemId, cantidad, motivo}`. Solo desde `DESPACHADA`: descuenta el importe de esas unidades y **no** repone stock. Si se devuelven todas, la comanda pasa a `ANULADA` (`estadoAlAnular: DESPACHADA`). |

Cada respuesta incluye `acciones`: las operaciones que admite el estado actual (`PREPARAR`, `DESPACHAR`,
`ANULAR`, `DEVOLVER`). La interfaz muestra solo esos botones. Una transición no permitida devuelve 409 sin cambios.
También trae `items[].id` y `cantidadDevuelta`, `devoluciones` (trago, cantidad, importe, motivo y fecha),
`totalDevuelto` y `totalNeto` (lo cobrado sin las devoluciones).

## Inventario: compras y movimientos

| Método | Ruta | Descripción |
|---|---|---|
| POST | `{bar}/compras` | `{insumoId, envases}` → 201 con el movimiento. Suma `envases × contenidoEnvase` y valoriza al costo actual. |
| GET | `{bar}/movimientos?limite=20` | Compras, consumos por comanda y ajustes de auditoría, del más reciente al más antiguo. |

## Auditoría

| Método | Ruta | Descripción |
|---|---|---|
| POST | `{bar}/auditorias` | `{responsable, conteos: [{insumoId, envasesCompletos, restante, comentario}]}` → 201. El físico es `envasesCompletos × contenidoEnvase + restante` (`restante` no puede superar un envase). Lleva cada insumo al valor contado, registra el ajuste y valoriza la merma (diferencia negativa × costo). |
| GET | `{bar}/auditorias` | Cierres con su detalle por insumo, el contenido del envase y el comentario. |

## Reportes (Decorator)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `{bar}/reportes?desde=&hasta=&turno=&secciones=GRAFICO,TENDENCIAS,MERMAS,MARGEN` | `ReporteBase` envuelto por un decorador por cada sección, en el orden pedido. `composicion` lista la cadena aplicada. |
| GET | `/api/reportes?establecimientos=id1,id2&desde=&hasta=&turno=&secciones=` | Lo mismo para varios bares juntos (sin `establecimientos`, todos). |
| GET | `/api/reportes/diario?establecimientos=&jornada=AAAA-MM-DD` | Jornada completa con `total`, `turnos` (`MEDIODIA`, `NOCHE`) y `ranking`. Sin `jornada`, la actual. |

Las fechas son **jornadas**: de 6:00 a 5:59 del día siguiente, así una venta del 2/11 a las 23 y otra del 3/11 a
las 2 quedan en la jornada del 2/11. Sin `desde`/`hasta` se toman las últimas 7 jornadas. `turno` filtra por el
horario de cada bar; lo registrado fuera de horario cuenta en el turno más cercano. La respuesta trae `periodo`,
`periodoAnterior` (misma duración, para la tendencia) y `establecimientos`. Tendencias compara contra
`periodoAnterior` y desglosa `porJornada`, `porEstablecimiento` y `porTrago`; Mermas agrega `envasesPerdidos` y
los `comentarios` del recuento.

## Alertas y traza

| Método | Ruta | Descripción |
|---|---|---|
| GET | `{bar}/notificaciones?limite=20` | Avisos emitidos por `AlertaStock` mediante `NotificacionFactory` (email, SMS y WhatsApp simulados). |
| GET | `{bar}/traza` | Últimos eventos de patrones (en memoria; se pierden al reiniciar). Solo registra operaciones confirmadas. |

## Ejemplo: flujo completo

```http
POST /api/establecimientos/{bar}/comandas
{"origen": "CLIENTE", "cliente": "Mesa 4", "items": [{"recetaId": "…", "cantidad": 2}]}

POST /api/establecimientos/{bar}/comandas/{id}/preparar
POST /api/establecimientos/{bar}/comandas/{id}/despachar
```

Si al despachar falta un insumo, la respuesta es 409 con `faltantes` (por ejemplo `"Gin nacional: faltan 100 ml"`),
no se descuenta ningún insumo y la comanda sigue `EN_PREPARACION`.
