# Plan de trabajo — BarStock Solutions

Objetivo: trabajar entre cinco personas en paralelo, con archivos y contratos definidos, y entregar un flujo demostrable:
**registrar comanda → preparar → despachar → descuento de stock correcto**.

El reparto es **un líder a cargo de la base común y la integración, más cuatro frentes de programación (A–D)**.
Los nombres se completan con el equipo; este documento no asigna personas ni registra aprobaciones que todavía no ocurrieron.

> **Estado al 30/10/2026:** la infraestructura de Hito 0 está preparada y verificada localmente según el README del repositorio.
> La migración `V1__esquema_inicial.sql` crea solamente el esquema `barstock`. Los contratos de negocio, las tablas,
> los datos de demostración, el logging común y las pruebas de integración del negocio siguen pendientes.
> El [ADR-001](decisiones/ADR-001-reglas-de-despacho.md) se incorpora como **PROPUESTA**, no como acuerdo aprobado.

---

## 0. Plazos reales (Actividad 3)

| Fecha de 2026 | Qué pasa |
|---|---|
| **Mar 3/11** | **Entrega:** informe de hasta 10 páginas, video de hasta 5 minutos, material de presentación y código que se defiende. |
| Mié 4/11, 23:59 | Fin de la disponibilidad en la plataforma. No se usa como margen de entrega. |
| Jue 26/11 | Presentación y defensa del TPO. |

| Día | Objetivo |
|---|---|
| **Jue 29 – Vie 30** | Conservar la infraestructura ya preparada y completar la base compartida: contratos, OpenAPI, revisión del ADR, esquema de negocio, datos y logging. Cada frente empieza cuando están listos los contratos que consume. |
| **Vie 30 – Sáb 31** | **Primera integración:** una comanda despachada contra PostgreSQL real, desde una pantalla mínima. El líder agrega y ejecuta las primeras pruebas de integración. |
| **Dom 1** | Completar los seis patrones, reportes sobre ventas reales y escenarios negativos. Redactar las secciones del informe a partir del código existente. |
| **Lun 2** | Corregir defectos, probar desde un clon limpio, revisar el código y ensayar el video. **Congelamiento de código a las 22:00.** |
| **Mar 3** | Revisión final de informe, video y diapositivas; `git tag entrega-tp` y subida. **Meta: antes de las 18:00.** |

**A confirmar con el docente:** si el código puede evolucionar entre el 3/11 y el 26/11. Hasta tener respuesta se prepara
la defensa sobre la versión entregada, sin prometer mejoras posteriores como parte de esa entrega.

### Alcance mínimo para el 3/11

| Prioridad | Tareas | Criterio |
|---|---|---|
| **Imprescindible** | A1–A9, B1–B5, C1–C7, C9–C10, D1–D7, E1–E5, E9–E10 y pruebas de integración del líder | Seis patrones implementados y testeados; flujo con PostgreSQL y Docker; pantalla Bartender que registra, prepara, despacha y muestra el stock resultante. |
| **Deseable** | A10 (movimientos de stock), B6–B7 (auditoría y recuento físico), C8 (cortesías), E6 (pantalla de compras y recetas) | Se toman cuando el flujo imprescindible funciona integrado. Compras y recetas se pueden demostrar por API si no hay pantalla de Encargado. |
| **Recortado** | D8 (ranking y margen por trago), E7, E8 y módulo predictivo | Trabajo futuro declarado en el informe; no se promete en el video. |

Si falta tiempo, se recorta en este orden: **E6 → C8 → A10 → B6–B7 y recuento en pantalla**.
Se conservan los seis patrones, CI mínimo y las pruebas del descuento atómico y del doble despacho.

---

## 1. Propiedad de archivos y trabajo en paralelo

El líder prepara los contratos compartidos y consolida las firmas públicas con los dueños de cada frente.
Cada frente implementa sus archivos y escribe sus tests unitarios. No espera la implementación de otro: usa mocks
o datos en memoria que respeten el mismo contrato. Los mocks se reemplazan a medida que llega cada implementación real.

| Archivo o área | Quién lo modifica | Cómo colaboran los demás |
|---|---|---|
| `pom.xml`, `application*.yml`, Docker, scripts, Makefile, CI y configuración de logging | Líder | Proponen el cambio necesario; el líder lo incorpora. |
| Contratos comunes: `Consumo`, `UnidadMedida`, excepciones base y firmas compartidas | Líder, con revisión del frente que los consume | Cada frente implementa contra ellos; un cambio se avisa antes de programarlo. |
| `docs/api/openapi.yaml`, formato común de errores y ejemplo de logging | Líder | A–D revisan sus endpoints y B valida los ejemplos para la pantalla. |
| Inventario, compras, notificaciones, sus repositorios, controladores y DTOs | A | C consume las operaciones de A; no las reimplementa. |
| Recetas, auditoría opcional, sus repositorios, controladores y DTOs; `frontend/` completo | B | Consume contratos de A/C y usa la API simulada mientras los endpoints no están listos. |
| `Comanda`, sus estados, repositorio, `ComandaController`, DTOs y **`BarStockFacade`** | **Solo C** | A/B/D entregan sus operaciones; C conecta las delegaciones y estrategias en sus propios archivos. |
| Precios, reportes, `FuenteDeVentas`, su adaptador de persistencia, `ReporteController` y DTOs | D | C integra la estrategia en `Comanda`; D consulta ventas sin editar las clases de C. |
| Tests unitarios de cada frente | A, B, C o D según el código probado | El dueño corrige su implementación y mantiene sus pruebas. |
| `backend/src/test/java/ar/edu/uade/barstock/integration/` | Líder | Cada frente aporta escenarios y corrige los fallos de su código; el líder mantiene la suite. |
| Migraciones de cada tabla después de la base común | Frente dueño de la tabla | Coordina la versión pendiente con el líder antes de integrar. |

**Solo C edita `Comanda` y `BarStockFacade`, también al incorporar las firmas iniciales.** El líder entrega las firmas
acordadas y los contratos comunes; C los incorpora en esos archivos. A, B y D no necesitan modificarlos para avanzar.
Lo compartido es un archivo concreto con dueño, no todo el directorio de repositorios o DTOs.

### Migraciones Flyway

- **Se conserva V1 sin modificar:** ya fue ejecutada y crea el esquema `barstock`, no las tablas de negocio.
- El esquema mínimo de insumos, recetas, ingredientes, comandas e ítems se acuerda con A/B/C. El líder incorpora la
  base común en una migración **V2**, si sigue siendo la siguiente disponible; las siguientes usan V3, V4… según el orden de integración.
- Se renumeran únicamente migraciones **pendientes de integrar** cuando otra ocupó esa versión. Las migraciones integradas
  o ya aplicadas en una base compartida son inmutables; una corrección se agrega como otra migración.
- No se asignan rangos por frente ni se activa `outOfOrder` para resolver conflictos de numeración.
- Un `pull` no requiere borrar la base: Flyway aplica las nuevas migraciones al arrancar. El líder debe actualizar la
  expectativa de versión 1 que hoy tiene `ArranqueIT` cuando incorpore V2 y posteriores; no se cambia esa prueba en esta adaptación documental.
- `make reset-db` o `./scripts/barstock.ps1 reset-db` se usan solo de forma explícita sobre una **base local descartable**.
  Borran todos sus datos. No forman parte de la rutina después de cada `pull`.

---

## 2. Arranque y decisiones pendientes

El arranque se completa a distancia; el lunes queda para correcciones y ensayo. Basta una lista compartida de tareas
o las propias PRs con responsable, dependencia y revisor. No se exige crear un tablero nuevo para poder programar.

- [ ] Completar los nombres del líder y de los cuatro frentes, los enlaces del equipo y del repositorio.
- [ ] Confirmar el stack y las versiones del [README](../README.md), conservando la infraestructura que ya funciona.
- [ ] Revisar el [ADR-001 propuesto](decisiones/ADR-001-reglas-de-despacho.md) y registrar qué decisiones se aceptan,
  cuáles cambian y cuáles siguen abiertas. No marcarlo aprobado por el solo hecho de agregar el archivo.
- [ ] Entregar los contratos que necesita la primera tarea de cada frente y elegir quién revisa su primera PR.

| Decisión | Propuesta a revisar | Afecta a |
|---|---|---|
| Momento del descuento | Descontar al despachar, en una única transacción. | A y C |
| Anulación antes y después del despacho | Antes no hay descuento; después no se repone stock y deja de computarse el ingreso. Motivo obligatorio. La merma por preparación fallida queda como limitación propuesta. | A, C y D |
| Comandos y transiciones | `/preparar`, `/despachar` y `/anular`; revisar también `Registrada → Anulada` y las respuestas 409 a repeticiones. | B y C |
| Números y unidades | `BigDecimal`, cantidades `NUMERIC(12,3)`, montos `NUMERIC(12,2)` y unidad explícita, según el ADR. | A–D |
| Estrategia y precio histórico | Resolver la tensión del ADR entre `calcularTotal()` invocando la estrategia y el precio fijado al registrar. Propuesta a confirmar: guardar el precio por ítem y sumar esos importes al consultar, sin ejecutar otra vez la estrategia. | C y D |
| Margen mínimo de Happy Hour | Definir si el margen se expresa sobre costo o sobre precio de venta, su valor y un ejemplo con el redondeo acordado. El ADR no resuelve esta fórmula. | D, revisa C |
| Concurrencia de otros comandos | Revisar cómo compiten preparación, anulación, despacho y compras. El ADR detalla el despacho; falta confirmar bloqueo o actualización condicional para las demás operaciones. | A y C, prueba el líder |
| Umbral de alerta y auditoría | Acordar el límite de reposición; la tolerancia del recuento físico solo bloquea la auditoría deseable. | A y B |

Cada frente puede avanzar en lo independiente y en tests contra contratos mientras se resuelve una decisión ajena.
Solo la implementación de la regla afectada espera su definición; no se detiene a todo el equipo.

---

## 3. Contratos y cierre de la base compartida

Las siguientes son las interfaces objetivo para concretar durante el Hito 0. Las firmas completas, tipos, retornos y errores
se entregan en los archivos del proyecto; esta tabla no afirma que ya estén implementados ni que las propuestas del ADR estén aprobadas.

| Contrato | Implementa | Lo consumen |
|---|---|---|
| `Consumo(insumoId, cantidad, unidad)`, `UnidadMedida`, excepciones base | Líder prepara la base común | A, B, C |
| `Inventario`: `consultarStock`, `descontarStock`, `descontarTodo(List<Consumo>)`, observadores y reposición | A | B y C |
| `Observador`, `Notificacion`, `NotificacionFactory` | A | Inventario y alertas |
| `Receta.calcularCosto()` y `Receta.consumos(cantidadesElegidas)` | B | C y D |
| `EstadoComanda`, operaciones de preparación/anulación y `BarStockFacade` | Solo C | Controladores y pruebas de integración |
| `EstrategiaPrecio.calcular(VentaPorReceta)` | D | C |
| `Reporte.generar()` y `FuenteDeVentas` | D | Reportes y su controlador |
| API REST: JSON, errores, validaciones y ejemplos en `docs/api/openapi.yaml` | Líder mantiene el contrato; cada frente implementa su API | B y líder de integración |

`descontarTodo` y la unidad en `Consumo` amplían el UML previo: se revisan con el ADR y se actualiza el diagrama
cuando se acepte el diseño. El descuento de varios insumos debe ser todo o nada.

### API objetivo

| Método | Ruta | Responsable / alcance |
|---|---|---|
| `GET` | `/api/insumos` y `/api/insumos/{id}/stock` | A; imprescindible |
| `POST` | `/api/compras` | A; imprescindible |
| `GET` / `POST` | `/api/recetas` | B; imprescindible |
| `GET` | `/api/recetas/{id}` | B; detalle de receta |
| `POST` | `/api/comandas` | C; imprescindible |
| `POST` | `/api/comandas/{id}/preparar`, `/despachar`, `/anular` | C; comandos propuestos en ADR-001 |
| `POST` / `GET` | `/api/recuentos` y `/api/auditoria` respectivamente | B; deseable |
| `GET` | `/api/reportes?tipo=` | D; imprescindible, aunque no tenga pantalla propia |

Los controladores delegan los casos de uso en Facade. C incorpora las delegaciones a operaciones de A/B/D a medida que
se conectan. El líder mantiene OpenAPI; B mantiene los mocks del frontend que lo consumen. Las rutas y respuestas
de negocio todavía deben implementarse; los endpoints de estado existentes no equivalen a este contrato completo.

### Criterio de salida por frente

- [ ] Puede levantar el entorno siguiendo el README.
- [ ] Sus contratos dependientes compilan y tienen tipos, retornos y errores definidos.
- [ ] Puede ejecutar sus tests unitarios con mocks sin esperar implementaciones ajenas.
- [ ] Tiene ejemplos de los JSON y datos que necesita, una primera tarea y un revisor de PR.

El Hito 0 compartido se completa cuando también están CI, esquema de negocio, datos reproducibles, OpenAPI y logging
de referencia. **No es una barrera global:** quien cumple sus dependencias empieza mientras el líder termina el resto.

---

## 4. Reparto de tareas

### Líder — Base común, logging e integración

**Responsable:** ________. Los IDs E se conservan para mantener la referencia con la versión anterior; no existe un quinto frente E.

| ID / tarea | Entrega | Dependencias |
|---|---|---|
| E1 | Conservar y completar repo, Docker Compose, Dockerfiles, `.env.example`, scripts y Makefile existentes. | Infraestructura ya preparada |
| E2 | Mantener CI mínimo desde Hito 0: build y tests de backend, build de frontend y ejecución de la suite de integración. | E1 |
| E3 | Configurar perfiles, Flyway, logging común, filtro `requestId`/MDC y manejo común de errores; incorporar el esquema de negocio en V2 o siguiente libre. | E1 y contratos de A/B/C |
| E9 | Preparar datos de desarrollo reproducibles para insumos y recetas, ejemplos equivalentes para mocks/tests/demo y activación del perfil `dev`. | Esquema de negocio de E3 |
| E10 | Mantener OpenAPI, contratos compartidos, formato común de errores y un ejemplo de logging. Entregar los contratos por partes según la primera tarea de cada frente. | Revisión breve con A–D; no espera a que existan sus implementaciones |
| Pruebas de integración | Escribir y mantener casos felices e infelices con PostgreSQL real: flujo completo, rollback, estados, concurrencia, precios históricos y errores de notificación. | Contratos primero; implementar cada escenario cuando llega el código real |

El líder define el convenio de logging y su configuración. **Cada frente instrumenta su propio código** siguiendo el ejemplo:
SLF4J, INFO para operaciones confirmadas, WARN para rechazos de negocio o fallos recuperables y ERROR para fallos inesperados.
Se incluyen `requestId` y, cuando corresponde, `comandaId`, insumo, cantidad y stock resultante; no se registran contraseñas
ni cuerpos completos. La propuesta del ADR exige éxito y alertas después del commit, no antes de una posible reversión.
Los logs de aplicación no sustituyen el registro persistente de movimientos A10, que sigue siendo deseable.

**Datos de demostración pendientes:** E9 debe lograr que el modo de desarrollo active el perfil `dev` y cargue los datos de
forma reproducible, sin duplicarlos al reiniciar. No se afirma que el comando actual ya cargue insumos o recetas.
Una vez completado E9, el procedimiento desde la raíz será:

```powershell
# Solo si todavía no existe .env:
Copy-Item .env.example .env
./scripts/barstock.ps1 dev
```

En macOS/Linux se usa `cp .env.example .env` si hace falta y `make dev`. Para repetir desde cero se puede ejecutar
explícitamente `reset-db` antes de `dev`, solo si se acepta perder la base local. Los datos se documentan con cantidades
conocidas para comprobar descuentos y alertas; las pruebas usan su propia base aislada.

### Frente A — Inventario, compras, Observer y Simple Factory

**Responsable:** ________. Archivos: inventario, notificaciones, sus repositorios, `InventarioController`, `CompraController` y DTOs.

| ID | Tarea | Depende de |
|---|---|---|
| A1 | `Insumo`: unidad, costo, punto de reorden y `BigDecimal`. | Contratos comunes |
| A2 | Consultar/descontar stock y rechazar cantidades o stock inválidos. | A1 |
| A3 | `descontarTodo`: descuentos atómicos, validación por insumo, unidades y orden estable; instrumentación y tests unitarios. | A2; contrato `Consumo` |
| A4 | Registrar compra y aumentar stock sin perder actualizaciones concurrentes. | A2; regla de concurrencia revisada |
| A5 | Observer: `Observador`, alta de observadores y notificación. | A2 |
| A6 | `AlertaStock` al alcanzar el umbral acordado. | A5 |
| A7 | Factory de Email/SMS con implementaciones de prueba que loguean. | Contrato de notificaciones |
| A8 | Aislar fallos de notificación: WARN con causa, sin deshacer el descuento confirmado; tests unitarios. | A6, A7 |
| A9 | Persistencia JPA y API de inventario/compras contra el esquema común; DTOs y manejo de errores del frente. | A1, E3; C conecta Facade |
| A10 | `MovimientoStock` persistente para compras y descuentos; agregado al UML sujeto a aprobación. **Deseable.** | A3, A4 |

A entrega sus operaciones a C y corrige su código si falla la integración. No edita Facade ni los tests de integración del líder.

### Frente B — Recetas y frontend Bartender mínimo

**Responsable:** ________. Archivos: recetas, auditoría opcional, sus repositorios/controladores/DTOs y `frontend/`.
La prioridad de pantalla es el flujo mínimo; no se incorpora una UI de Encargado antes de integrarlo.

| ID | Tarea | Depende de |
|---|---|---|
| B1 | `IngredienteReceta`: cantidades mínima, máxima y por defecto. | Contratos comunes |
| B2 | Validar cantidades elegidas. | B1 |
| B3 | Calcular costo con costos de insumos simulados. | B1; contrato de A |
| B4 | Traducir una receta a `List<Consumo>` con cantidad y unidad. | B2 |
| B5 | Repositorio, API de alta/listado/detalle, DTOs y tests unitarios de recetas. | B1, E3; C conecta Facade |
| E4 | Cliente en `frontend/src/api/` y API simulada sencilla contra OpenAPI. | Contrato de E10 |
| E5 | Registrar comanda, preparar, despachar y consultar stock resultante; mostrar errores de la API. Conectar cada endpoint real cuando esté listo. | E4; A/B/C para integración |
| B6 | Recuento físico y comparación con stock teórico. **Deseable.** | A2; tolerancia definida |
| B7 | API y reporte de auditoría; recuento en pantalla. **Deseable.** | B6 |
| E6 | Pantalla de compras y recetas. **Deseable.** | E4; API de A/B |
| E7 | Pantalla de auditoría y reportes. **Recortado.** | B7, D7 |
| E8 | Ruteo y guardas por rol. **Recortado.** | E5 |

B puede desarrollar la pantalla contra mocks sin esperar al backend. El líder mantiene OpenAPI; B informa los ajustes
necesarios y mantiene los mocks, sin añadir reglas de negocio al frontend.

### Frente C — Comandas, State, Facade y su API

**Responsable:** ________. Es el único editor de `Comanda`, estados, `BarStockFacade`, sus repositorios/controlador/DTOs.

| ID | Tarea | Depende de |
|---|---|---|
| C1 | `Comanda` y `VentaPorReceta`, con persistencia del precio por ítem según la decisión que se acepte. | Contratos comunes |
| C2 | State: `Registrada`, `EnPreparacion`, `Despachada`, `Anulada` y comandos explícitos. | C1; reglas aceptadas |
| C3 | Transiciones válidas e inválidas con tests por estado y logging. | C2 |
| C4 | Doble despacho sin doble descuento, con respuesta 409 conforme al ADR si se aprueba. | C2; concurrencia definida |
| C5 | Registrar comanda por Facade y conectar las delegaciones a operaciones de A/B/D. | C1 y firmas de los frentes |
| C6 | Despachar en una transacción: agregar consumos por insumo, usar `descontarTodo` y cambiar estado. | C2; A3 y B4 mediante mocks al desarrollar |
| C7 | Anular con motivo según la decisión aceptada; coordinar la concurrencia con preparación y despacho. | C2; revisión del ADR |
| C8 | Cortesía: consume stock y no se cobra. **Deseable.** | C6 |
| C9 | Repositorio y `ComandaController`: registrar, preparar, despachar y anular; DTOs y errores. | C1, E3 |
| C10 | Conectar Strategy y fijar el precio al registrar si se aprueba la propuesta; total histórico desde importes guardados, sin recalcular con la estrategia actual. | C1, D1; D revisa |

C prueba con mocks de A/B/D y conecta sus implementaciones a medida que están disponibles. El líder cubre la transacción
real y los pedidos simultáneos en integración; C conserva sus tests unitarios y corrige sus fallos de implementación.

### Frente D — Strategy, Decorator y reportes

**Responsable:** ________. Archivos: precios, reportes, fuente de ventas, adaptador de lectura, controlador y DTOs propios.

| ID | Tarea | Depende de |
|---|---|---|
| D1 | `EstrategiaPrecio` y `PrecioNormal`. | Contrato de precio |
| D2 | Happy Hour con reloj inyectable y margen mínimo definido mediante ejemplo. | D1; fórmula de margen acordada |
| D3 | Demanda alta con factor de incremento. | D1 |
| D4 | Entregar estrategias y ejemplos a C; revisar C10 y la conservación del precio histórico. | D1–D3 |
| D5 | `Reporte`, `ReporteBase` y contrato `FuenteDeVentas`; comenzar con datos en memoria. | Contrato de lectura de ventas |
| D6 | Decorator: gráfico/tendencias y test unitario de composición. | D5 |
| D7 | Adaptador real de `FuenteDeVentas` sobre comandas persistidas y API `/api/reportes`; excluir anuladas según la regla aceptada. Reemplazar la fuente simulada en la ejecución final. | D5, E3 y lectura del modelo persistido de C; C conecta Facade |
| D8 | Ranking y margen por trago. **Recortado.** | Dominio integrado |

D no espera las clases reales de C para probar precios o decoradores. La integración final de D7 sí usa ventas reales,
sin cambiar `Comanda` ni su repositorio: el adaptador de lectura pertenece a D y respeta el esquema acordado.

---

## 5. Dependencias, integración y aceptación

```text
Base del líder: contratos + OpenAPI + errores/logging + esquema/datos + CI
        │ entrega por dependencia, sin una espera global
        ├── A: inventario/compras ────────────┐
        ├── B: recetas ──────────────────────┼── C: comanda + State + Facade
        ├── D: estrategias ──────────────────┘             │
        └── B: frontend con mocks ─────────────── API real ─┘
                          D: reportes con fuente simulada → adaptador de ventas reales
                          Líder: pruebas de integración felices e infelices
```

El camino crítico sigue siendo **A3 + B4 → C6**; E5 demuestra el resultado en pantalla. C10 conecta los precios de D.
Las dependencias inevitables se resuelven con contratos y mocks al desarrollar, y con integración real desde el viernes/sábado.

| Fase | Trabajo comprometido |
|---|---|
| **Hito 0, completar vie 30** | Líder: E1–E3/E9/E10; A–D revisan los contratos y el esquema que consumen. Conservar lo que ya está preparado. |
| **Vie 30 – sáb 31** | A1–A4/A9, B1–B5/E4–E5, C1–C6/C9 y conexión inicial C10 con D1; D1–D3/D5. Líder: primeras pruebas de integración. |
| **Dom 1** | A5–A8, C7 y cierre de C10, D4/D6–D7; líder completa escenarios negativos y de concurrencia. Solo después se toman deseables. |
| **Lun 2 – mar 3** | Correcciones, clon limpio, ensayo, congelamiento del lunes a las 22:00, informe, video y entrega. |

### Pruebas de integración del líder

El líder escribe, ejecuta y mantiene la suite con PostgreSQL real. Los frentes aportan los datos del escenario y corrigen
sus propios archivos; no se asigna al líder reimplementar el negocio cuando una prueba falla.

- **Caso feliz:** datos conocidos → registrar → preparar → despachar → estado, importe y stock persistidos correctos.
- **Consumos:** más de un trago con el mismo insumo; suma correcta y descuento de todos los ingredientes.
- **Casos infelices:** stock insuficiente, unidad o cantidad inválida, comanda inexistente y transición inválida;
  respuestas acordadas y ausencia de descuentos parciales o cambios de estado indebidos.
- **Repetición y concurrencia:** doble despacho secuencial y simultáneo; dos comandas compitiendo por el mismo insumo;
  compra concurrente con descuento; preparación/anulación frente a despacho, según las reglas que se definan.
- **Efectos posteriores:** fallo de notificación sin rollback; ningún éxito o alerta por una transacción revertida.
- **Precio y anulación:** precio histórico sin recalcular; efecto de la anulación en stock e ingresos según la decisión aceptada.

### Criterios de aceptación antes del congelamiento

1. El modo `dev` carga los datos documentados desde una base vacía y no los duplica al reiniciar.
2. El Bartender registra, prepara y despacha desde la interfaz; consulta el stock real resultante y ve los errores de la API.
3. Cada ingrediente baja exactamente lo consumido; si falta uno, no baja ninguno y la comanda no queda despachada.
4. Los pedidos repetidos o simultáneos no descuentan dos veces; las comandas distintas no dejan stock negativo.
5. Las alertas y sus errores respetan la transacción, el umbral y el convenio de logging aceptados.
6. Los seis patrones tienen implementación y tests unitarios; C/D verifican precios y D demuestra composición de reportes sobre ventas reales.
7. Las pruebas de integración felices e infelices pasan y la demostración se repite siguiendo el README desde un clon limpio.

---

## 6. Reglas de trabajo

- Cada tarea o PR tiene responsable y otra persona como revisor; no hace falta añadir otra herramienta de seguimiento.
- PRs pequeñas y verificables, con CI verde. El dueño incorpora los cambios en sus archivos; solo C modifica Facade y Comanda.
- Para cambiar un contrato, primero avisar al consumidor y al líder; OpenAPI, mocks y firmas deben seguir siendo compatibles.
- Los tests unitarios de patrones se ejecutan sin levantar Spring ni base de datos. El líder mantiene las pruebas de integración.
- Cada frente incorpora logging y manejo de errores en su código siguiendo el ejemplo común, y corrige sus defectos de integración.
- Si una dependencia bloquea, se avisa en el momento y se sigue con mocks; no se espera a la reunión del lunes.
- Una decisión de diseño aceptada actualiza el ADR y los `.puml` en la misma PR. Las propuestas no aceptadas siguen identificadas como tales.

---

## 7. Informe, video y defensa

**Informe: máximo 10 páginas.** Cada frente escribe sobre su código real; el líder aporta base común y pruebas de integración.
Durante viernes/domingo se reúnen fragmentos y resultados; el lunes se unifica el estilo. La distribución es un máximo,
no una obligación de llenar las diez páginas.

| Sección | Páginas | Contenido |
|---|---|---|
| Portada y resumen | 1 | Equipo, problema, propuesta y alcance entregado |
| Arquitectura | 1,5 | MVC + Facade, diagrama, stack y Docker |
| Patrones de diseño | 3,5 | Problema, clases reales, fragmento y test por patrón |
| Datos y persistencia | 1 | PostgreSQL, esquema y migraciones reales V1/V2 y posteriores |
| Reglas críticas | 1 | Atomicidad, estados, concurrencia, precios históricos y anulaciones aceptadas |
| Pruebas | 1 | Cómo se corren unitarias/integración y resultados reales |
| Limitaciones y futuro | 0,5 | Auditoría/UI recortada y propuestas que no se implementaron |
| Conclusiones y anexos | 0,5 | Aprendizajes, enlaces al repo y video |

**Video: hasta 5 minutos y participación de los cinco.** Distribuir las voces durante el ensayo; el bloque de demo puede
repartirse entre integrantes para que la intervención de cada uno sea cercana a un minuto.

| Tiempo | Contenido |
|---|---|
| 0:00–0:45 | Problema y propuesta |
| 0:45–1:45 | Arquitectura y entorno Docker |
| 1:45–3:30 | Demo: registrar → preparar → despachar → stock por ingrediente → alerta |
| 3:30–4:30 | Patrones en el código y evidencia breve de pruebas |
| 4:30–5:00 | Limitaciones, trabajo futuro y cierre |

Grabar el lunes por la noche o martes por la mañana sobre el código congelado, después de ensayar.
El material de presentación acompaña la entrega del 3/11 y se usa en la defensa del 26/11.
En la reunión del lunes cada integrante explica una parte que no programó: cualquiera puede ser consultado sobre todo el sistema.

### Checklist de entrega

- [ ] Informe PDF de 10 páginas o menos y video de hasta 5 minutos.
- [ ] Diapositivas/material de presentación listo para entregar el 3/11.
- [ ] README con comandos y enlaces reales; ADR con su estado real, sin presentar propuestas como aprobaciones.
- [ ] Pruebas unitarias y de integración ejecutadas; resultado y limitaciones documentados.
- [ ] Arranque con `docker compose up --build` y demo reproducible con el modo `dev`, probados desde clon limpio.
- [ ] `git tag entrega-tp` sobre la versión defendible y ZIP del código si la plataforma lo solicita.
- [ ] Informe, video y subida tienen un responsable elegido antes del cierre; no se asume que los hace automáticamente el líder.
- [ ] Subida completada antes de las 18:00 del martes 3/11.
