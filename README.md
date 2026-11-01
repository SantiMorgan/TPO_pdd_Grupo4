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

> **Estado:** base ejecutable más CRUD de inventario. Incluye creación, consulta, edición, baja lógica y reactivación de insumos, API y persistencia PostgreSQL con `BigDecimal`. Ver [contrato y ejemplos de inventario](docs/api/inventario.md) y [UML implementado](docs/uml/Inventario_Implementado.puml). El stock se carga al crear; descuentos y compras quedan pendientes. La base incluye una pantalla inicial por rol y los endpoints `/api` y `/api/health`. Lo descripto en las secciones de arquitectura es el diseño acordado;
> las funcionalidades se marcan como implementadas en [Roadmap](#7-roadmap) recién cuando existan y estén testeadas.
> El reparto y las tareas están en el [plan de trabajo](docs/PLAN_DE_TRABAJO.md). Las reglas de despacho están propuestas en [ADR-001](docs/decisiones/ADR-001-reglas-de-despacho.md), pendiente de aprobación.

---

## 1. ¿Qué hace la aplicación?

Un sistema de inventario básico solo registra altas y bajas de botellas cerradas. BarStock Solutions usa un modelo de
**stock fraccionado por recetas**: cada vez que se despacha un trago, descuenta de forma atómica los mililitros exactos
de cada insumo que lo compone. Esto permite comparar el stock teórico con el físico y detectar mermas no registradas.

**Roles**

| Rol | Qué hace |
|---|---|
| **Bartender** | Registra comandas, despacha tragos, consulta stock en tiempo real y carga el recuento físico al cierre de turno. |
| **Encargado** | Gestiona insumos, recetas y compras; audita mermas; consulta reportes de gestión. |

**Capacidades principales**

- **Inventario:** insumos con unidad de medida, costo unitario y punto de reorden; registro de compras; descuento atómico por receta.
- **Recetas:** ingredientes con cantidad mínima/máxima (tolerancia) y cálculo del costo teórico.
- **Comandas:** ciclo de vida `Registrada → EnPreparacion → Despachada` con posibilidad de `Anulada`.
- **Precios:** normal, Happy Hour y demanda alta (intercambiables en tiempo de ejecución).
- **Auditoría:** stock teórico vs. recuento físico, con detección de desvíos.
- **Alertas y reportes:** aviso automático al llegar al punto de reposición; reportes de gestión componibles.

---

## 2. Arquitectura

**MVC simple con Facade**, tal como se documentó en la Actividad 2:

```
 React (Vista)  ──(HTTP/JSON)──▶  Controladores REST  ──▶  BarStockFacade  ──▶  Modelo de dominio ──▶ PostgreSQL
 BartenderView / EncargadoView        (Controlador)         (orquestación)      (lógica + patrones)
```

| Capa MVC | Dónde vive | Responsabilidad |
|---|---|---|
| **Vista** | `frontend/` (React) | Dos interfaces diferenciadas por rol. Sin lógica de negocio. |
| **Controlador** | `backend/.../controller` | Recibe requests, valida formato, delega en `BarStockFacade`. Sin lógica de negocio. |
| **Facade** | `backend/.../facade` | Punto de entrada único a los casos de uso (`registrarComanda`, `despacharComanda`, `registrarCompra`, `auditarInventario`). Solo orquesta. |
| **Modelo** | `backend/.../model` | Entidades, reglas de negocio y los 6 patrones de diseño. Persistencia incluida (`model/persistence`). |

**Patrones de diseño (documentados)**

| Patrón | Uso | Paquete |
|---|---|---|
| State | Ciclo de vida de `Comanda` | `model/comanda/state` |
| Strategy | `EstrategiaPrecio` (Normal, HappyHour, DemandaAlta) | `model/precio` |
| Observer | `Inventario` → `AlertaStock` | `model/inventario` |
| Simple Factory | `NotificacionFactory.crear(tipo)` (Email, SMS) | `model/notificacion` |
| Facade | `BarStockFacade` | `facade` |
| Decorator | `Reporte` → `ReporteConGrafico`, `ReporteConTendencias` | `model/reporte` |

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

El árbol describe la arquitectura objetivo. `EstadoController` y `BarStockFacade` publican el estado
de la aplicación. El CRUD implementado sigue `InventarioController` → `BarStockFacade` → `InventarioService`
→ `InsumoRepository`, con el dominio en `model/inventario`. La migración V1 crea el
esquema `barstock`, V2 incorpora la tabla `insumo` y V3 restringe las cantidades a enteros
(stock y punto de reorden, tanto en ML como en UNIDAD); los costos conservan dos decimales.
El prototipo de consola original se conserva en `src/` y no
forma parte del build de Spring Boot. Los diagramas y el PDF existente están en `docs/uml/` y `docs/entregas/`.

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
| 0 — contratos compartidos | Contratos Java, OpenAPI, tablas de negocio, datos de ejemplo, logging común y casos de integración | ⏳ pendiente |
| Inventario — CRUD | Alta, consulta, edición, baja lógica y reactivación de insumos; validaciones, persistencia y pruebas | ✅ implementado; contrato en `docs/api/inventario.md` |
| 1 | Dominio base: `Insumo`, `Inventario`, `Receta`, `IngredienteReceta` + descuento atómico | ⏳ pendiente |
| 2 | Flujo completo: registrar comanda → despachar → descuento de stock (State + Facade) | ⏳ pendiente |
| 3 | Strategy de precios + Observer/AlertaStock + Simple Factory | ⏳ pendiente |
| 4 | Compras, recuento físico y auditoría | ⏳ pendiente |
| 5 | Reportes (Decorator) y vistas React por rol | ⏳ pendiente |

---

## 8. Decisiones abiertas (cerrar antes de implementar el comportamiento afectado)

Las propuestas están en [ADR-001: reglas de despacho](docs/decisiones/ADR-001-reglas-de-despacho.md).
El archivo se incorpora como propuesta; su inclusión no aprueba sus reglas. El plan enumera los puntos que deben
cerrarse antes de implementar cada contrato, incluido el precio histórico, el margen de Happy Hour y la concurrencia.

1. **Anulación post-despacho:** ¿se repone el stock? ¿se revierte el ingreso? El UML permite `Despachada → Anulada` pero no define el efecto.
2. **Momento del descuento de stock:** ¿al pasar a `EnPreparacion` o a `Despachada`? Hay que fijarlo para evitar doble descuento.
3. **Precisión numérica:** reemplazar `double` del UML por `BigDecimal` para cantidades (ml) y montos.
4. **Estrategia de precio:** el UML detallado la declara tanto en `Comanda` como en `Venta`; debe quedar en un único lugar.
5. **Autenticación por rol:** el UML no define login; hasta confirmarlo, la separación Bartender/Encargado es solo de interfaz.
6. **¿El backend corre dentro de Docker durante el desarrollo diario?** La base soporta ambos modos; `make dev` incluye recompilación y DevTools dentro de Docker.

---

## Documentación de diseño

- `docs/entregas/` — Actividad 1, Actividad 2 e infograma.
- `docs/uml/` — diagramas PlantUML (general y por patrón).
- [Plan de trabajo](docs/PLAN_DE_TRABAJO.md) — reparto, tareas, dependencias y pruebas a cargo del líder.
- [ADR-000: base ejecutable](docs/decisiones/ADR-000-base-ejecutable.md) — registro del andamiaje existente.
- [ADR-001: reglas de despacho](docs/decisiones/ADR-001-reglas-de-despacho.md) — propuesta de reglas de negocio.
- `docs/decisiones/` — registro de decisiones de arquitectura.
