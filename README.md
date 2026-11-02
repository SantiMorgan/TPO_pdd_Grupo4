# BarStock Solutions

Sistema de gestión y control de inventarios gastronómicos para bares y restaurantes de coctelería de autor.
Trabajo Práctico — *Proceso de Desarrollo de Software*, UADE (Grupo 4).

## Equipo

| Integrante | GitHub | LinkedIn |
|---|---|---|
| Morganti, Santiago | [@usuario](https://github.com/USUARIO) | [Perfil](https://www.linkedin.com/in/USUARIO) |
| Araujo, Nicolás Ezequiel | [@usuario](https://github.com/USUARIO) | [Perfil](https://www.linkedin.com/in/USUARIO) |
| Alonso, María Eugenia | [@usuario](https://github.com/USUARIO) | [Perfil](https://www.linkedin.com/in/USUARIO) |
| Welz, Mathias Gustavo | [@usuario](https://github.com/USUARIO) | [Perfil](https://www.linkedin.com/in/USUARIO) |
| Vazquez, Lucas Valentin | [@usuario](https://github.com/USUARIO) | [Perfil](https://www.linkedin.com/in/USUARIO) |

**Repositorio:** <https://github.com/SantiMorgan/TPO_pdd_Grupo4>
<!-- Completar los perfiles de los integrantes. -->


**Docente:** Bueno, Moisés Evaristo · **Materia:** Proceso de Desarrollo de Software · **Universidad:** Universidad Argentina de la Empresa (UADE)

> **Estado:** aplicación completa con los seis patrones implementados y testeados, y una vista por rol (Cliente,
> Bartender, Encargado y Dueño) sobre varios establecimientos. Flujo comanda → preparación → despacho con descuento
> atómico en ml, precios dinámicos, alertas de reposición, compras, auditoría de mermas y reportes componibles.
> 110 pruebas unitarias y 19 de integración con PostgreSQL real. Ver la [API](docs/api/endpoints.md), el
> [UML implementado](docs/uml/BarStock_Implementado.puml) y las decisiones de [ADR-002](docs/decisiones/ADR-002-roles-y-establecimientos.md)
> (roles, establecimientos y reglas del [ADR-001](docs/decisiones/ADR-001-reglas-de-despacho.md) que se aplicaron; ambos pendientes de aprobación del equipo).
> El reparto original está en el [plan de trabajo](docs/PLAN_DE_TRABAJO.md).

---

## 1. ¿Qué hace la aplicación?

Un sistema de inventario básico solo registra altas y bajas de botellas cerradas. BarStock Solutions usa un modelo de
**stock fraccionado por recetas**: cada vez que se despacha un trago, descuenta de forma atómica los mililitros exactos
de cada insumo que lo compone. Esto permite comparar el stock teórico con el físico y detectar mermas no registradas.

**Roles**

Al ingresar se elige el establecimiento y el rol. Cada rol ve solo sus pantallas (guarda en `frontend/src/routes`).

| Rol | Vistas | Qué hace |
|---|---|---|
| **Cliente** | Carta · Mis pedidos | Ve el menú con el precio vigente (Happy Hour, demanda alta), hace su pedido y sigue su estado en vivo. |
| **Bartender** | Barra / comandas · Stock · Cierre de turno | Recibe los pedidos de clientes, registra comandas, las prepara y despacha; consulta stock y carga el recuento físico. |
| **Encargado** | Inventario y compras · Auditoría · Alertas | Gestiona insumos, compra envases, audita mermas y revisa los avisos de reposición. |
| **Dueño** | Resumen · Reportes · Precios · Menú · Establecimientos | Compara sus bares, arma reportes, configura la política de precios, el menú y los canales de alerta. |

**Capacidades principales**

- **Inventario:** insumos con unidad, costo unitario, contenido por envase y punto de reorden; compras por envase; descuento atómico por receta; historial de movimientos.
- **Recetas:** ingredientes con su cantidad y cálculo del costo teórico; baja lógica del menú.
- **Comandas:** ciclo de vida `Registrada → EnPreparacion → Despachada`, con `Anulada` (y devolución después del despacho).
- **Precios:** normal, Happy Hour (con margen mínimo) y demanda alta, intercambiables en tiempo de ejecución.
- **Auditoría:** stock teórico vs. recuento físico, ajuste y merma valorizada.
- **Alertas y reportes:** aviso al cruzar el punto de reposición (panel, email, SMS o WhatsApp simulados); reportes componibles.
- **Varios establecimientos:** cada bar con su inventario, menú, comandas, precios y alertas.

---

## 2. Arquitectura

**MVC simple con Facade**, tal como se documentó en la Actividad 2:

```
 React (Vista)  ──(HTTP/JSON)──▶  Controladores REST  ──▶  BarStockFacade  ──▶  Modelo de dominio ──▶ PostgreSQL
 Cliente / Bartender /                (Controlador)         (orquestación)      (lógica + patrones)
 Encargado / Dueño
```

| Capa MVC | Dónde vive | Responsabilidad |
|---|---|---|
| **Vista** | `frontend/` (React) | Cuatro interfaces por rol con guarda de rutas. Habla solo con `/api` por `src/api/client.js`. Sin lógica de negocio. |
| **Controlador** | `backend/.../controller` | Recibe requests, valida formato con DTOs, delega en `BarStockFacade`. `ApiExceptionHandler` traduce errores a `ProblemDetail`. |
| **Facade** | `backend/.../facade` | Punto de entrada único a los casos de uso (`registrarComanda`, `despacharComanda`, `registrarCompra`, `auditarInventario`, `consultarCarta`, `generarReporte`…). Solo orquesta y define la transacción. |
| **Modelo** | `backend/.../model` | Entidades, reglas de negocio y los 6 patrones de diseño. Persistencia incluida (`model/persistence`). |

**Patrones de diseño (implementados y testeados)**

| Patrón | Clases | Dónde se ve en la app | Test |
|---|---|---|---|
| State | `EstadoComanda` → `Registrada`, `EnPreparacion`, `Despachada`, `Anulada` (`model/comanda/state`) | Los botones del bartender salen de `acciones` del estado; el cliente sigue los pasos | `EstadoComandaTest` |
| Strategy | `EstrategiaPrecio` → `PrecioNormal`, `PrecioHappyHour`, `PrecioDemandaAlta`; `SelectorEstrategiaPrecio` (`model/precio`) | Precio de la carta y de cada comanda; el dueño fuerza o automatiza la estrategia | `EstrategiaPrecioTest`, `SelectorEstrategiaPrecioTest` |
| Observer | `Inventario` (sujeto) → `ObservadorStock`: `AlertaStock`, `BitacoraStock` (`model/inventario`) | Alerta de reposición al despachar, comprar o auditar | `InventarioTest`, `AlertaStockTest` |
| Simple Factory | `NotificacionFactory.crear(TipoNotificacion)` → `NotificacionPanel`, `Email`, `Sms`, `WhatsApp` (`model/notificacion`) | Canales de alerta configurados por bar | `NotificacionFactoryTest` |
| Facade | `BarStockFacade` (`facade`) | Despacho: coordina Comandas, Recetas e Inventario en una transacción | `BarStockFacadeTest`, `FlujoComandaIT` |
| Decorator | `Reporte` → `ReporteBase`; `ReporteDecorator` → `ReporteConGrafico`, `ConTendencias`, `ConMermas`, `ConMargen` (`model/reporte`) | El dueño elige las secciones y ve la cadena aplicada | `ReporteDecoratorTest` |

También se aplican **MVC**, **Repository** (Spring Data) y **DTO** para el acceso a datos y el intercambio entre capas,
y GRASP: Experto (`Receta.calcularCosto()`, `Comanda.calcularTotal()`), Creador (`Comanda` crea sus `ItemComanda`),
Controlador (controladores REST), Fabricación pura (`TrazaPatrones`, `BitacoraNotificaciones`) y Variaciones protegidas
(`EstrategiaPrecio`, `Notificacion`, `FuenteDatosReporte`). El botón **◈ Patrones** de la app muestra la traza real de
cada patrón mientras se usa.

---

## 3. Stack y requisitos

| Componente | Versión | Notas |
|---|---|---|
| **Java (JDK)** | **21 LTS** | Obligatorio solo si se desarrolla fuera de Docker. |
| **Maven** | 3.9.11 (wrapper incluido en `backend/`: `sh ./mvnw`) | No hace falta instalarlo. |
| **Spring Boot** | 3.5.16 | API REST, inyección de dependencias, Spring Data JPA. |
| **PostgreSQL** | **16** | Se levanta por Docker; no instalar localmente. |
| **Flyway** | incluido en Spring Boot | Migraciones versionadas del esquema (`db/migration`). |
| **React** | 18.3 + Vite 7 | Se compila dentro de Docker. |
| **Node.js** | 22 LTS (22.12+) | Solo para desarrollo del frontend fuera de Docker. |
| **Docker** | Engine 24+ y Compose 2.24.4+ | **Único requisito obligatorio para correr todo el sistema.** Docker Desktop debe usar contenedores Linux. |
| **JUnit 5 + Mockito** | — | Tests unitarios; Testcontainers para integración con PostgreSQL. |

> Para correr el proyecto alcanza con **Docker + Git**. El JDK y Node solo se necesitan si se quiere trabajar fuera de contenedores.

---

## 4. Estructura de carpetas

```text
barstock-solutions/
├── README.md
├── docker-compose.yml            # db + backend + frontend
├── docker-compose.dev.yml        # override de desarrollo (puertos expuestos, hot reload)
├── .env.example                  # variables de entorno (copiar a .env)
├── Makefile                      # atajos: make up / down / test / reset-db
├── .github/
│   ├── workflows/ci.yml          # build + tests en cada PR
│   └── pull_request_template.md  # checklist de cada PR
│
├── docs/
│   ├── PLAN_DE_TRABAJO.md        # líder, cuatro frentes, contratos y pruebas
│   ├── uml/                      # BarStock_Diagrama_General.puml, BarStock_Diagramas_Patrones.puml
│   ├── entregas/                 # PDFs de Actividad 1 y 2, infograma
│   └── decisiones/               # ADRs (una decisión por archivo)
│
├── infra/
│   └── nginx/nginx.conf          # sirve el frontend y proxea /api al backend
│
├── backend/
│   ├── Dockerfile                # multi-stage: build con Maven → runtime con JRE 21
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/ar/edu/uade/barstock/
│       │   │   ├── BarStockApplication.java
│       │   │   │
│       │   │   ├── controller/              # ── CONTROLADOR ──
│       │   │   │   ├── ComandaController.java
│       │   │   │   ├── InventarioController.java
│       │   │   │   ├── CompraController.java
│       │   │   │   ├── AuditoriaController.java
│       │   │   │   ├── ReporteController.java
│       │   │   │   ├── dto/                 # request/response (no exponen el dominio)
│       │   │   │   └── error/               # @ControllerAdvice, mapeo de excepciones a HTTP
│       │   │   │
│       │   │   ├── facade/                  # ── FACADE ──
│       │   │   │   └── BarStockFacade.java
│       │   │   │
│       │   │   ├── model/                   # ── MODELO ──
│       │   │   │   ├── inventario/          # Insumo, Inventario, Observador, AlertaStock   (Observer)
│       │   │   │   ├── receta/              # Receta, IngredienteReceta
│       │   │   │   ├── comanda/             # Comanda, VentaPorReceta
│       │   │   │   │   └── state/           # EstadoComanda, Registrada, EnPreparacion,
│       │   │   │   │                        # Despachada, Anulada                           (State)
│       │   │   │   ├── precio/              # EstrategiaPrecio, PrecioNormal,
│       │   │   │   │                        # PrecioHappyHour, PrecioDemandaAlta            (Strategy)
│       │   │   │   ├── notificacion/        # Notificacion, Email, SMS, NotificacionFactory,
│       │   │   │   │                        # TipoNotificacion                              (Simple Factory)
│       │   │   │   ├── reporte/             # Reporte, ReporteBase, ReporteDecorator,
│       │   │   │   │                        # ReporteConGrafico, ReporteConTendencias       (Decorator)
│       │   │   │   ├── auditoria/           # recuento físico vs. teórico, mermas
│       │   │   │   ├── persistence/         # repositorios (Spring Data) y mapeo a BD
│       │   │   │   └── exception/           # excepciones de dominio (StockInsuficiente, TransicionInvalida…)
│       │   │   │
│       │   │   └── config/                  # beans, wiring de observadores, CORS
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/            # V1__esquema_inicial.sql, V2__…
│       │
│       └── test/java/ar/edu/uade/barstock/  # espeja la estructura de main/
│           ├── model/…                      # unit tests por patrón (sin Spring, sin BD)
│           └── integration/                 # flujos completos con Testcontainers
│
└── frontend/
    ├── Dockerfile                # multi-stage: build con Node → nginx
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.jsx
        ├── api/                  # cliente HTTP hacia /api (único punto de contacto con el backend)
        ├── views/
        │   ├── bartender/        # BartenderView: registrar venta, stock, recuento físico
        │   └── encargado/        # EncargadoView: compras, recetas, auditoría, reportes
        ├── components/           # componentes reutilizables
        └── routes/               # ruteo y guardas por rol
```

El árbol muestra la estructura implementada. Además de los paquetes del árbol, el modelo tiene
`model/establecimiento` (bar, `PoliticaPrecio` y `ConfiguracionAlertas`) y `model/traza` (`TrazaPatrones`).
Los controladores son `EstablecimientoController`, `InventarioController`, `CompraController`, `RecetaController`,
`CartaController`, `ComandaController`, `AuditoriaController`, `ReporteController` y `SeguimientoController`.
El frontend tiene una carpeta por rol en `views/` (`cliente`, `bartender`, `encargado`, `dueno`) y la sesión en `session/`.

Migraciones: V1 crea el esquema, V2 la tabla `insumo`, V3 restringe las cantidades a enteros, V4 agrega
`establecimiento` y asigna cada insumo a un bar, V5 recetas y comandas, y V6 movimientos, auditorías y notificaciones.
Los datos de ejemplo están en `db/demo/R__datos_demo.sql` y se cargan con el perfil `demo`.
El prototipo de consola original se conserva en `src/` y no forma parte del build de Spring Boot.
Los diagramas están en `docs/uml/` (el implementado es `BarStock_Implementado.puml`) y las entregas en `docs/entregas/`.

**Reglas de dependencia (se validan en code review):**

1. `controller` → `facade` → `model`. Nunca al revés.
2. `model` **no** depende de `controller`, `facade` ni de DTOs.
3. Los controladores no contienen reglas de negocio; el Facade no contiene reglas de negocio (solo orquesta).
4. El frontend habla únicamente con `/api` a través de `src/api/`.
5. Los paquetes de patrones (`state`, `precio`, `notificacion`, `reporte`) se testean sin levantar Spring ni la BD.

---

## 5. Infraestructura con Docker

Todo el entorno se levanta con un comando. No hay que instalar PostgreSQL, JDK ni Node.

| Servicio | Imagen | Puerto | Descripción |
|---|---|---|---|
| `db` | `postgres:16-alpine` | 5432 (solo en dev) | Volumen persistente `pgdata`, *healthcheck* con `pg_isready`. |
| `backend` | build de `backend/Dockerfile` | 8080 | Espera a que `db` esté *healthy*; Flyway migra el esquema al arrancar. |
| `frontend` | build de `frontend/Dockerfile` | 3000 | nginx sirve React y proxea `/api` → `backend:8080`. |

**Primer arranque**

```bash
git clone https://github.com/SantiMorgan/TPO_pdd_Grupo4.git
cd TPO_pdd_Grupo4
cp .env.example .env
docker compose up --build
```

- Aplicación: <http://localhost:3000>
- API: <http://localhost:8080/api>
- Salud de la API y PostgreSQL: <http://localhost:8080/api/health>

Docker Compose activa el perfil `demo`, que carga **Bar Ámbar** y **La Esquina** con los insumos y recetas del
prototipo (Campari queda cerca de su punto de reposición para mostrar la alerta con un Negroni). Para una base sin
datos de ejemplo, poné `SPRING_PROFILES_ACTIVE=` vacío en `.env`. Para volver a los datos iniciales, `make reset-db`
(borra la base local) y otra vez `make up`.

**Demo sugerida:** abrí dos pestañas. En una entrá como **Cliente** y pedí un Negroni; en la otra como **Bartender**:
el pedido aparece solo, se prepara y se despacha, y el cliente ve el cambio de estado. Después pasá por **Encargado**
(compra y recuento físico) y **Dueño** (Happy Hour forzado y reporte con decoradores). La guía completa está en el
botón **📋 Guía** de la aplicación.

En PowerShell, desde la carpeta del repositorio:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

La primera ejecución descarga imágenes y dependencias. Los puertos se pueden cambiar en `.env`.

**Comandos habituales**

```bash
make up          # levanta todo en segundo plano
make down        # apaga los contenedores (conserva datos)
make reset-db    # apaga y borra el volumen de PostgreSQL (BD limpia)
make logs        # sigue los logs de todos los servicios
make test        # corre los tests del backend dentro de un contenedor
make dev         # compose + override de desarrollo (puertos de BD expuestos, hot reload)
```

Si no tenés Make instalado, usá `./scripts/barstock.ps1 up` en PowerShell. Acepta las mismas acciones:
`up`, `down`, `reset-db`, `logs`, `test` y `dev`. `reset-db` elimina los datos locales de PostgreSQL.

`make dev` sirve Vite en el mismo puerto 3000 y recompila los cambios de `backend/src/` para que DevTools
reinicie Spring. Los cambios de dependencias o de Dockerfiles requieren volver a ejecutar el comando.
`make down` también apaga el entorno de desarrollo. `make test` ejecuta `mvn verify` dentro de un contenedor
con acceso al socket Docker para que Testcontainers cree su propia base PostgreSQL de prueba.

**Desarrollo fuera de Docker (opcional)**

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d db
cd backend
sh ./mvnw spring-boot:run
# En otra terminal, desde la raíz:
cd frontend
npm ci
npm run dev
```

En Windows usá `./mvnw.cmd` en lugar de `sh ./mvnw`. El backend local usa PostgreSQL en `localhost:5432`
con los valores de `.env.example`; si los cambiaste, exportá `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` antes de iniciarlo. El frontend local abre
en <http://localhost:5173> y Vite redirige `/api` a `localhost:8080`.

Dentro de `backend/`, `sh ./mvnw test` corre las pruebas unitarias sin Docker y `sh ./mvnw verify` agrega
la prueba de arranque, migración y API con PostgreSQL real vía Testcontainers (requiere Docker activo).
CI ejecuta ambas, compila el frontend y comprueba el arranque de Compose en cada PR.

**Variables de entorno (`.env.example`)**

```env
POSTGRES_DB=barstock
POSTGRES_USER=barstock
POSTGRES_PASSWORD=cambiar_en_local
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/barstock
BACKEND_PORT=8080
FRONTEND_PORT=3000
DB_PORT=5432
```

`.env` está en `.gitignore`; solo se versiona `.env.example`.

---

## 6. Trabajo en equipo (5 integrantes)

**Modalidad:** un líder prepara la base compartida y coordina la integración; los otros cuatro integrantes implementan
los frentes A–D del [plan de trabajo](docs/PLAN_DE_TRABAJO.md). Los nombres se completan al repartir las tareas.

| Responsable | Alcance |
|---|---|
| **Líder** | Contratos comunes, OpenAPI, infraestructura, esquema inicial de negocio, configuración y convención de logs, integración y pruebas de integración de casos felices e infelices. |
| **A** | Inventario, compras, Observer, Simple Factory y sus controladores/repositorios. |
| **B** | Recetas y pantalla mínima del bartender, incluido su cliente de API y mocks. |
| **C** | Comandas, State, `ComandaController` y `BarStockFacade`. Conecta los contratos de A, B y D. |
| **D** | Estrategias de precios, reportes, Decorator y fuente real de datos de reportes. |

- Cada frente edita sus archivos y escribe sus pruebas unitarias. C mantiene `Comanda` y `BarStockFacade`; D entrega las estrategias y C las conecta.
- El líder define el logging y prepara su configuración y ejemplo; cada frente incorpora los mensajes de negocio siguiendo esa convención.
- El líder escribe y mantiene las pruebas de integración. Cada frente corrige los fallos de su implementación que esas pruebas detecten.
- Los cambios de contratos, OpenAPI, configuración y numeración de migraciones se coordinan con el líder. Una colaboración se asigna a un archivo o tarea concreta.
- El seguimiento puede hacerse con un mensaje fijado y PRs: responsable, tarea, dependencia, criterio de aceptación y revisor. Jira o Projects son opcionales.
- Cada frente empieza cuando están disponibles los contratos de los que depende; usa mocks para avanzar sin esperar otras implementaciones.
- Las decisiones acordadas se registran en `docs/decisiones/`. ADR-001 conserva el estado **PROPUESTA** hasta que se apruebe; sus alternativas no se consideran reglas ya aceptadas.

**Flujo de Git**

- `main` protegida: solo se integra por Pull Request con al menos 1 aprobación de otro integrante y CI en verde.
- Ramas: `feature/<frente>-<descripcion>` (por ejemplo, `feature/a-inventario`), `fix/<descripcion>`.
- Commits en formato *Conventional Commits* (`feat:`, `fix:`, `test:`, `docs:`, `chore:`).
- Una PR = un cambio chico y verificable. Si cambia el diseño, se actualiza el `.puml` en la misma PR.

**Definición de terminado:** comportamiento definido, código implementado, tests escritos y pasando, integrado con la
arquitectura, UML actualizado si corresponde.

El logging operativo usa SLF4J hacia `stdout`, con contexto `requestId` y, cuando corresponda, `comandaId`.
El líder define los niveles y eventos y prepara la infraestructura común; esta configuración específica todavía está
pendiente. El historial persistente de movimientos de stock es una mejora deseable, separada de los logs.

---

## 7. Roadmap

Según el [plan de trabajo](docs/PLAN_DE_TRABAJO.md), la entrega es el **3/11/2026** y la defensa el **26/11/2026**.
La primera integración de negocio se busca entre el viernes 30 y el sábado 31/10; el lunes 2 queda para correcciones
y ensayo, con congelamiento a las 22:00.

| Hito | Contenido | Estado |
|---|---|---|
| 0 — infraestructura | Andamiaje del repo, Docker Compose, CI, esquema inicial en Flyway | ✅ base implementada y verificada localmente; publicación compartida pendiente |
| 0 — contratos compartidos | Contratos Java, tablas de negocio, datos de ejemplo y casos de integración | ✅ contratos, migraciones V4–V6, datos `demo` y pruebas de integración. ⏳ OpenAPI formal y `requestId` en logs (la API está en `docs/api/endpoints.md`) |
| Inventario — CRUD | Alta, consulta, edición, baja lógica y reactivación de insumos; validaciones, persistencia y pruebas | ✅ implementado, ahora por establecimiento; contrato en `docs/api/inventario.md` |
| 1 | Dominio base: `Insumo`, `Inventario`, `Receta`, `IngredienteReceta` + descuento atómico | ✅ implementado y testeado |
| 2 | Flujo completo: registrar comanda → despachar → descuento de stock (State + Facade) | ✅ implementado; incluye rollback por falta de stock y doble despacho simultáneo |
| 3 | Strategy de precios + Observer/AlertaStock + Simple Factory | ✅ implementado y testeado |
| 4 | Compras, recuento físico y auditoría | ✅ implementado y testeado |
| 5 | Reportes (Decorator) y vistas React por rol | ✅ implementado: cuatro roles (Cliente, Bartender, Encargado, Dueño) |
| Futuro | Inicio de sesión real, envío real de email/SMS, predicción de demanda | ⏳ fuera del alcance de la entrega |

---

## 8. Decisiones (implementadas como propuesta, pendientes de aprobación del equipo)

Las reglas están en [ADR-001](docs/decisiones/ADR-001-reglas-de-despacho.md) y lo que se aplicó en el código, en
[ADR-002](docs/decisiones/ADR-002-roles-y-establecimientos.md). Si el equipo cambia alguna, se cambia el código afectado.

1. **Anulación post-despacho:** es una devolución: el importe deja de contar como ingreso y **no** se repone stock.
2. **Momento del descuento de stock:** al pasar a `Despachada`, en una única transacción y con bloqueo de fila (sin doble descuento).
3. **Precisión numérica:** `BigDecimal` para cantidades enteras (ml o unidades) y montos con dos decimales.
4. **Estrategia de precio:** vive en `Comanda`; el precio se fija al registrar y se guarda por ítem.
5. **Autenticación por rol:** sigue sin login; los cuatro roles se separan en la interfaz con guarda de rutas.
6. **¿El backend corre dentro de Docker durante el desarrollo diario?** La base soporta ambos modos; `make dev` incluye recompilación y DevTools dentro de Docker.

---

## Documentación de diseño

- [Prototipo interactivo](docs/prototipos/BarStock_Solutions_Prototipo_Mejorado.html) — demo HTML independiente con datos en `localStorage`; se abre directamente en el navegador.
- `docs/entregas/` — Actividad 1, Actividad 2 e infograma.
- `docs/uml/` — diagramas PlantUML (general y por patrón). [BarStock_Implementado.puml](docs/uml/BarStock_Implementado.puml) refleja el código actual.
- [API](docs/api/endpoints.md) — todos los endpoints por establecimiento; [inventario](docs/api/inventario.md) en detalle.
- [ADR-002: roles y establecimientos](docs/decisiones/ADR-002-roles-y-establecimientos.md) — decisiones aplicadas en la aplicación.
- [Plan de trabajo](docs/PLAN_DE_TRABAJO.md) — reparto, tareas, dependencias y pruebas a cargo del líder.
- [ADR-000: base ejecutable](docs/decisiones/ADR-000-base-ejecutable.md) — registro del andamiaje existente.
- [ADR-001: reglas de despacho](docs/decisiones/ADR-001-reglas-de-despacho.md) — propuesta de reglas de negocio.
- `docs/decisiones/` — registro de decisiones de arquitectura.
