# CLAUDE.md — Memoria técnica del proyecto SIGA Backend

> Documento vivo. Se actualiza cada vez que se completa una parte importante.
> Última actualización: 2026-06-16.

## 1. Objetivo del proyecto

MVP funcional del **backend de SIGA** (Sistema Integrado de Gestión Aduanera), que
automatiza el cruce fronterizo terrestre del Paso Los Libertadores (Chile–Argentina).
La entidad central es `tramite_aduanero`, que agrupa documentos, vehículo, declaración
SAG, revisión PDI, validación con Aduana Argentina, notificaciones e historial de estados.

Fuente de verdad del modelo de datos: **`docs/sigaMER.md`**.

## 2. Stack técnico

- **Java 21**
- **Spring Boot 4.1.0** (starters modulares: `webmvc`, `data-jpa`, `flyway`, `security`,
  `security-oauth2-client`, `validation`, `actuator`)
- **Maven** (wrapper `mvnw` / `mvnw.cmd`, descarga Maven 3.9.16)
- **Neon Postgres** (runtime, serverless) + **H2** en memoria (solo para tests)
- **Docker** (imagen multi-stage) + **Render** (despliegue del backend)
- **Spring Data JPA** / Hibernate
- **Spring Security** + **OAuth2 Client (Login con Google / OIDC)**
- **Flyway** (migraciones)
- **Jakarta Validation** + **Lombok**
- Package base: `cl.duoc.siga.backend`

## 3. Estructura de carpetas

```
src/main/java/cl/duoc/siga/backend/
├── config/        SecurityConfig, CorsConfig
├── controller/    PublicController, AuthController, UsuarioController, TramiteController,
│                  DocumentoController, VehiculoController, DeclaracionSagController, RevisionPdiController
├── dto/request/   *Request (records con validación)
├── dto/response/  *Response (records)
├── enums/         13 enums del dominio
├── exception/     ResourceNotFoundException, ConflictException, ErrorResponse, GlobalExceptionHandler
├── mapper/        Mappers estáticos entidad -> response
├── model/         10 entidades JPA
├── repository/    10 repositorios Spring Data JPA
├── security/      SigaOidcUserService, OAuth2LoginSuccessHandler
└── service/       UsuarioService, TramiteService, DocumentoService, VehiculoService,
                   DeclaracionSagService, RevisionPdiService
src/main/resources/
├── application.properties          (config base por variables de entorno)
├── application-google.properties   (perfil google: OAuth2/OIDC por env vars)
└── db/migration/V1__init_schema.sql (esquema inicial Flyway)
src/test/resources/
└── application.properties          (perfil de test con H2)
Raíz: Dockerfile, .dockerignore, render.yaml (despliegue), application-local.properties.example
```

## 4. Entidades implementadas (según el MER)

| Entidad JPA | Tabla | Relaciones |
|---|---|---|
| `Usuario` | `usuario` | referenciada por casi todas (pasajero, funcionario, etc.) |
| `TramiteAduanero` | `tramite_aduanero` | `@ManyToOne` pasajero y funcionarioAsignado (Usuario) |
| `Documento` | `documento` | `@ManyToOne` tramite (1..* por trámite) |
| `Vehiculo` | `vehiculo` | `@OneToOne` tramite (0..1, FK única) |
| `DeclaracionSag` | `declaracion_sag` | `@OneToOne` tramite (0..1) + `@ManyToOne` funcionarioSag |
| `RevisionPdi` | `revision_pdi` | `@OneToOne` tramite (0..1) + `@ManyToOne` funcionarioPdi |
| `ValidacionAduanaArg` | `validacion_aduana_arg` | `@OneToOne` tramite (0..1) |
| `Notificacion` | `notificacion` | `@ManyToOne` tramite + `@ManyToOne` usuarioDestino |
| `HistorialEstadoTramite` | `historial_estado_tramite` | `@ManyToOne` tramite + `@ManyToOne` funcionario |
| `ReporteEstadistico` | `reporte_estadistico` | `@ManyToOne` generadoPor (Usuario) |

> No se añadieron entidades fuera del MER. La autenticación reutiliza `Usuario`
> (no se creó tabla `Rol` aparte; el discriminador `tipo_usuario` + `rol_funcionario` basta).

**Decisión de mapeo de relaciones:** las entidades NO declaran colecciones `@OneToMany`.
Los hijos se consultan por repositorio (`findByTramiteId`). El borrado en cascada lo
garantiza la FK `ON DELETE CASCADE` en la base de datos. Esto evita problemas de
`LazyInitializationException` (con `open-in-view=false`) y simplifica el MVP.

## 5. Endpoints REST

### Públicos (sin autenticación)
- `GET /api/public/health` — estado del servicio
- `GET /api/public/info` — info de la app
- `GET /actuator/health` — health de actuator
- `GET /oauth2/authorization/google` — **inicia el login con Google**

### Protegidos (requieren login con Google)
- `GET /api/me` — usuario autenticado
- **Usuarios:** `GET /api/usuarios`, `GET /api/usuarios/{id}`, `POST /api/usuarios`, `PUT /api/usuarios/{id}`, `DELETE /api/usuarios/{id}`
- **Trámites:** `GET /api/tramites` (filtros `?estado=` y `?pasajeroId=`), `GET /api/tramites/{id}`, `POST /api/tramites`, `PUT /api/tramites/{id}`, `DELETE /api/tramites/{id}`
  - `PATCH /api/tramites/{id}/estado` — cambia estado + registra historial
  - `GET /api/tramites/{id}/historial` — trazabilidad de estados
  - `GET /api/tramites/{id}/documentos` — documentos del trámite
- **Documentos:** `GET /api/documentos?tramiteId=`, `GET /api/documentos/{id}`, `POST /api/documentos`, `DELETE /api/documentos/{id}`
- **Vehículos:** `GET /api/vehiculos?tramiteId=`, `GET /api/vehiculos/{id}`, `POST /api/vehiculos`, `PUT /api/vehiculos/{id}`, `DELETE /api/vehiculos/{id}`
- **Declaración SAG:** `GET /api/declaraciones-sag?tramiteId=`, `GET /api/declaraciones-sag/{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`
- **Revisión PDI:** `GET /api/revisiones-pdi?tramiteId=`, `GET /api/revisiones-pdi/{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`

> `ValidacionAduanaArg`, `Notificacion` y `ReporteEstadistico` están modeladas
> (entidad + repositorio) pero aún sin controller REST. Ver Pendientes.

## 6. Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `SIGA_DB_URL` | *(default: URL de Neon)* | URL JDBC; override solo si cambias de BD |
| `SIGA_DB_USER` | *(default: `neondb_owner`)* | Usuario de la BD |
| `SIGA_DB_PASSWORD` | **REQUERIDA (único secreto)** | Contraseña de Neon — siempre por env var |
| `SPRING_PROFILES_ACTIVE` | *(vacío)* | Usar `google` en Render para habilitar OAuth2 Google |
| `GOOGLE_CLIENT_ID` | *(sin default)* | Client ID de Google OAuth2 |
| `GOOGLE_CLIENT_SECRET` | *(sin default)* | Client Secret de Google OAuth2 — siempre por env var |
| `SIGA_CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Orígenes CORS permitidos |
| `SIGA_FRONTEND_REDIRECT_URI` | `http://localhost:5173` | Redirección post-login |

**Conexión Neon Postgres (proyecto del curso):**
- **Project:** sigma-db · **Branch:** production · **Database:** neondb · **Role/User:** neondb_owner
- **Host (pooler):** `ep-proud-credit-atxikxie-pooler.c-9.us-east-1.aws.neon.tech`
- **JDBC URL:** `jdbc:postgresql://ep-proud-credit-atxikxie-pooler.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require`
- ⚠️ La **contraseña NO se guarda en el repo**: se entrega únicamente por la variable de
  entorno `SIGA_DB_PASSWORD`.

> **Sin secretos hardcodeados.** El login con Google vive en el perfil `google`
> (`application-google.properties`); si no se activa ese perfil, la app arranca igual
> con el login deshabilitado (útil para desarrollo).

## 7. Decisiones técnicas importantes

1. **ENUM PostgreSQL → VARCHAR + CHECK.** El MER define tipos ENUM nativos; se mapean
   como `VARCHAR` con restricciones `CHECK` y `@Enumerated(EnumType.STRING)` en JPA
   (recomendado por el propio MER para reducir fricción).
2. **JSONB → TEXT.** `validacion_aduana_arg.respuesta_externa` y
   `reporte_estadistico.parametros` se almacenan como `TEXT` (JSON serializado) para
   simplificar el mapeo en el MVP.
3. **`usuario.rut` y `usuario.password_hash` son NULLABLE.** El registro vía Google OAuth
   no provee RUT ni contraseña local; se completan después. (Adaptación de seguridad/auth
   permitida por el enunciado.)
4. **Flyway es la fuente de verdad del esquema.** En runtime
   `spring.jpa.hibernate.ddl-auto=validate`: Flyway crea/administra las tablas e Hibernate
   solo **valida** que las entidades calcen (nunca `update`, nunca `create`). En tests se usa
   H2 con `create-drop` y Flyway deshabilitado (por eso `validate` NO se ejerce en `clean test`,
   sino en el arranque real contra PostgreSQL/Neon).
   - **Base de datos: Neon Postgres** (serverless). `SIGA_DB_URL` incluye `?sslmode=require`,
     dialecto `PostgreSQLDialect` explícito, `show-sql=true` y pool Hikari pequeño
     (`maximum-pool-size=5`, `connection-timeout=30000`, `idle-timeout=10000`,
      `max-lifetime=30000`). `SIGA_DB_URL` y `SIGA_DB_USER` tienen default de Neon;
      `SIGA_DB_PASSWORD` es obligatorio y sin esa variable la app no arranca.
5. **Sin colecciones `@OneToMany`** (ver sección 4).
6. **Timestamps con `@CreationTimestamp` / `@UpdateTimestamp`** (Hibernate) en lugar de
   depender de los `DEFAULT now()` de la BD.
7. **Seguridad:** endpoints públicos vs protegidos separados; entry point devuelve `401`
    (API, sin redirección automática); login se inicia en `/oauth2/authorization/google`.
   - El perfil `google` lee `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` desde variables
     de entorno. El callback queda explícito como `{baseUrl}/login/oauth2/code/{registrationId}`.
   - En Render se usa `server.forward-headers-strategy=framework` para respetar
     `X-Forwarded-Proto`/`X-Forwarded-Host` y generar redirect URIs HTTPS correctas detrás
     del proxy de Render.
   - Para login cross-site desde Vercel hacia Render, la sesión `JSESSIONID` se configura
     con `SameSite=None`, `Secure=true` y `HttpOnly=true`. Esto permite que el frontend
     `https://siga-fronted.vercel.app` consuma `/api/me` usando `credentials: "include"`
     después del login Google.
8. **CSRF deshabilitado** para simplificar el consumo REST desde React/curl en el MVP
    (en producción se habilitaría con tokens).
9. **DTOs como `record`** y mappers estáticos (sin MapStruct) para mantener el código
   simple y sin dependencias extra.

## 8. Pendientes y próximos pasos

- [ ] Controllers/servicios para `ValidacionAduanaArg`, `Notificacion`, `ReporteEstadistico`.
- [ ] Patrón Observer: disparar `Notificacion` automáticamente en cada cambio de estado.
- [ ] Autorización por rol (`tipo_usuario` / `rol_funcionario`) sobre endpoints concretos.
- [ ] Validaciones de negocio por `tipo_tramite` (p. ej. VEHICULO exige vehículo).
- [ ] 2FA (flag `dos_fa_habilitado` ya existe en el modelo).
- [ ] Tests de integración por endpoint (más allá de `contextLoads`).
- [ ] Datos semilla (seed) para el demo.

## 9. Estado de verificación (2026-06-16)

- ✅ `./mvnw clean test` — **BUILD SUCCESS** (1/1, contexto Spring completo carga sobre H2)
  después de configurar OAuth2 Google para Render (`server.forward-headers-strategy=framework`
  y callback explícito `{baseUrl}/login/oauth2/code/{registrationId}`).
- ✅ `SPRING_PROFILES_ACTIVE=google ./mvnw test` con `GOOGLE_CLIENT_SECRET` dummy —
  **BUILD SUCCESS**; confirma que el perfil `google` carga la registración OAuth2 desde env vars.
- ✅ `./mvnw clean compile` — compila sin errores.
- ✅ `./mvnw clean test` — **BUILD SUCCESS** (1/1, contexto Spring completo carga sobre H2;
  valida las 10 entidades, mappings, seguridad y CORS).
- ✅ `./mvnw spring-boot:run` **contra Neon** — verificado en vivo (2026-06-15):
  Hikari conectó a Neon (PostgreSQL 18.4); **Flyway aplicó la migración v1** creando todas
  las tablas en `neondb` (`now at version v1`); Hibernate `validate` pasó sin errores;
  `GET /api/public/health` → **200 UP**; endpoints protegidos (`/api/me`, `/api/usuarios`)
  → **401** sin login (separación público/protegido correcta).
  Nota: el puerto 8080 lo ocupa Apache (`httpd`) local; la verificación se hizo en 8081
  (`$env:SERVER_PORT="8081"`). En condiciones normales la app usa 8080.
- ✅ **Artefacto de producción (Docker/Render)**: `mvnw clean package` genera el fat-jar y,
  ejecutado con `java -jar` + `PORT=8081` (mismo mecanismo que usa Render con su `$PORT`),
  conectó a Neon y respondió `health → 200` y `/api/me → 401`. Es lo que ejecuta la imagen
  Docker. Docker no está instalado localmente; la imagen se construye en Render.
- ✅ Validación de secretos: `application.properties` usa solo variables de entorno; sin
  credenciales hardcodeadas (verificado con grep en `src/`).
- ℹ️ Entorno detectado: **PostgreSQL 17** en `C:\Program Files\PostgreSQL\17\bin`, listener
  activo en el puerto 5432.
- 🔒 Para correr en vivo sin exponer la clave: perfil `local` con `application-local.properties`
  (gitignored; plantilla en `application-local.properties.example`), o arrancar desde el
  terminal del usuario con sus propias variables `$env:`.

## 10. Comandos para ejecutar y probar

```bash
# 1) Compilar y correr tests (usa H2, NO requiere PostgreSQL)
./mvnw clean test

# 2) Crear la base de datos en PostgreSQL (una vez)
#    psql -U postgres -c "CREATE DATABASE siga_db;"

# 3) Levantar la app contra NEON (Windows PowerShell):
$env:SIGA_DB_URL="jdbc:postgresql://ep-proud-credit-atxikxie-pooler.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require"
$env:SIGA_DB_USER="neondb_owner"
$env:SIGA_DB_PASSWORD="<TU_PASSWORD_DE_NEON>"
.\mvnw.cmd spring-boot:run

#    Alternativa local con PostgreSQL:
#    $env:SIGA_DB_URL="jdbc:postgresql://localhost:5432/siga_db"; $env:SIGA_DB_PASSWORD="..."
#    .\mvnw.cmd spring-boot:run

#    Opción segura para que Claude lo ejecute sin exponer la clave:
#    copiar application-local.properties.example -> application-local.properties (gitignored),
#    poner ahí la cadena de Neon, y arrancar con el perfil "local":
#    .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# 4) Con login de Google (perfil "google" + credenciales):
$env:GOOGLE_CLIENT_ID="xxxx.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET="xxxx"
$env:PORT="8081"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=google
```

En Linux/macOS: usar `export VAR=valor` y `./mvnw spring-boot:run`.

## 11. Despliegue en Render (Docker + Neon)

El backend se despliega en **Render** como servicio **Docker**, conectándose a **Neon**.
Las credenciales **no van en el repo ni en la imagen**: se cargan como variables de entorno
en el dashboard de Render (declaradas como `sync: false` en `render.yaml`).

**Archivos:**
- `Dockerfile` — multi-stage: build con Maven/JDK 21 → runtime con JRE 21 (usuario no-root).
- `.dockerignore` — excluye `target/`, secretos locales, etc.
- `render.yaml` — Blueprint con las env vars; los secretos quedan como `sync: false`.
- `server.port=${PORT:8080}` en `application.properties` — la app escucha el puerto que
  Render inyecta en `PORT` (local = 8080).

**Variables de entorno a configurar en Render → Environment:**

| Variable | Valor |
|---|---|
| `SIGA_DB_PASSWORD` | *(contraseña de Neon — **único requerido**, secreto)* |
| `SIGA_DB_URL` *(opcional)* | ya tiene default = URL de Neon; definir solo si cambias de BD |
| `SIGA_DB_USER` *(opcional)* | default `neondb_owner` |
| `SIGA_CORS_ORIGINS` | `https://siga-fronted.vercel.app,http://localhost:5173,http://localhost:3000` |
| `SPRING_PROFILES_ACTIVE` | `google` |
| `GOOGLE_CLIENT_ID` | Client ID público de Google OAuth2 |
| `GOOGLE_CLIENT_SECRET` | Client Secret de Google OAuth2 (**secreto**, no versionar) |
| `SIGA_FRONTEND_REDIRECT_URI` | `https://siga-fronted.vercel.app/inicio` |

**Configuración actual Vercel + Render:**
- Frontend: `https://siga-fronted.vercel.app`
- Backend: `https://siga-backend-cs0t.onrender.com`
- `SIGA_CORS_ORIGINS=https://siga-fronted.vercel.app,http://localhost:5173,http://localhost:3000`
- `SIGA_FRONTEND_REDIRECT_URI=https://siga-fronted.vercel.app/inicio`
- La cookie de sesión se envía como `SameSite=None; Secure; HttpOnly` para permitir
  requests cross-site con `credentials: "include"`.

**Pasos:**
1. Subir el repo a GitHub.
2. Render → **New → Blueprint** (usa `render.yaml`) **o** New → Web Service → Docker.
3. Cargar las variables de entorno de arriba (`SIGA_DB_PASSWORD` como secreto).
4. Health check path: `/api/public/health`.

> **Troubleshooting:** si el log de Render muestra
> `Driver ... claims to not accept jdbcUrl, ${SIGA_DB_URL}` o `No open ports detected`,
> revisa que `SIGA_DB_PASSWORD` esté cargada y que no hayas sobreescrito `SIGA_DB_URL` con un
> placeholder. Corrige las variables en Render → Environment y haz *Manual Deploy*.
> El puerto lo gestiona Render vía `PORT` (la app ya lo lee
> con `server.port=${PORT:8080}`); no hay que configurarlo a mano.

**Build local de la imagen (cuando tengas Docker instalado):**
```bash
docker build -t siga-backend .
docker run -p 8080:8080 -e PORT=8080 \
  -e SIGA_DB_URL="jdbc:postgresql://<host>.neon.tech/neondb?sslmode=require" \
  -e SIGA_DB_USER="neondb_owner" \
  -e SIGA_DB_PASSWORD="<password>" \
  siga-backend
```

**Probar local (con el backend en http://localhost:8081 si usas OAuth local):**
- Públicos: `curl http://localhost:8081/api/public/health`
- Login Google: abrir en navegador `http://localhost:8081/oauth2/authorization/google`
  (tras autenticar, redirige al frontend; la sesión queda en la cookie `JSESSIONID`).
- `GET /api/me` y los CRUD requieren esa sesión (usar el navegador o pasar la cookie a curl).

**Configurar Google OAuth2:** en Google Cloud Console crear credenciales OAuth 2.0 con
estos redirect URIs autorizados:
- Producción Render: `https://siga-backend-cs0t.onrender.com/login/oauth2/code/google`
- Local 8081: `http://localhost:8081/login/oauth2/code/google`

**Probar OAuth2 en Render + Vercel:** abrir `https://siga-fronted.vercel.app/ingresar`, iniciar
login con Google, volver a `https://siga-fronted.vercel.app/inicio` y verificar que el frontend
haga `GET https://siga-backend-cs0t.onrender.com/api/me` con `credentials: "include"` y reciba
`200` con el usuario autenticado. Si se prueba directo en backend, abrir
`https://siga-backend-cs0t.onrender.com/oauth2/authorization/google`; Spring crea/recupera el
`Usuario` por correo en la tabla `usuario` y redirige a `SIGA_FRONTEND_REDIRECT_URI`.
