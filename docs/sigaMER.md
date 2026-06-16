# SIGA — Modelo Entidad-Relación

> **Contexto para Claude Code**
> SIGA (Sistema Integrado de Gestión Aduanera) automatiza el cruce fronterizo terrestre en el Paso Los Libertadores (Chile–Argentina). Integra Aduanas, PDI, SAG y Aduana Argentina. Entidad central: `tramite_aduanero`, que agrupa documentos, datos de vehículo, declaración SAG, revisión PDI y validación externa.
>
> **Stack objetivo:** PostgreSQL 16 + (Spring Boot/JPA *o* Node/Prisma) + React.
> Este archivo es la **fuente de verdad del esquema**. Genera entidades/migraciones a partir del DDL de abajo, no de la prosa.
>
> **Reglas de dominio que el código debe respetar:**
> 1. Un trámite siempre pertenece a UN pasajero y tiene un `folio` único.
> 2. `tipo_tramite` determina qué tablas hijas aplican: `MENORES` → documentos; `VEHICULO` → vehículo + documentos; `SAG` → declaración SAG. Todo trámite puede tener documentos.
> 3. Todo trámite genera control PDI y, cuando hay cruce real, validación con Aduana Argentina.
> 4. Cada cambio de estado se registra en `historial_estado_tramite` (trazabilidad = requisito RNF y munición para el demo).
> 5. `usuario` es tabla única con discriminador `tipo_usuario`; el `rol_funcionario` solo aplica a funcionarios (tal como define el diagrama de clases del informe).

---

## 1. Diagrama ER (Mermaid)

```mermaid
erDiagram
    USUARIO ||--o{ TRAMITE_ADUANERO : "inicia (pasajero)"
    USUARIO ||--o{ TRAMITE_ADUANERO : "atiende (funcionario)"
    USUARIO ||--o{ REVISION_PDI : "ejecuta"
    USUARIO ||--o{ DECLARACION_SAG : "revisa"
    USUARIO ||--o{ REPORTE_ESTADISTICO : "genera"
    USUARIO ||--o{ NOTIFICACION : "recibe"

    TRAMITE_ADUANERO ||--o{ DOCUMENTO : "contiene"
    TRAMITE_ADUANERO ||--o| VEHICULO : "declara"
    TRAMITE_ADUANERO ||--o| DECLARACION_SAG : "incluye"
    TRAMITE_ADUANERO ||--o| REVISION_PDI : "es controlado por"
    TRAMITE_ADUANERO ||--o| VALIDACION_ADUANA_ARG : "se valida con"
    TRAMITE_ADUANERO ||--o{ NOTIFICACION : "dispara"
    TRAMITE_ADUANERO ||--o{ HISTORIAL_ESTADO_TRAMITE : "registra"

    USUARIO {
        bigint id PK
        varchar rut UK
        varchar nombre
        varchar correo UK
        varchar password_hash
        enum tipo_usuario "PASAJERO|FUNCIONARIO|ADMINISTRADOR"
        enum rol_funcionario "ADUANAS|PDI|SAG (null si no es funcionario)"
        enum estado_cuenta "ACTIVA|DESHABILITADA"
        boolean dos_fa_habilitado
        timestamptz fecha_creacion
    }

    TRAMITE_ADUANERO {
        bigint id PK
        varchar folio UK
        bigint pasajero_id FK
        bigint funcionario_asignado_id FK "nullable"
        enum tipo_tramite "MENORES|VEHICULO|SAG"
        enum estado "BORRADOR|PENDIENTE|EN_REVISION|PENDIENTE_VALIDACION_EXTERNA|OBSERVADO|APROBADO|RECHAZADO"
        varchar url_comprobante "nullable, se genera al aprobar"
        timestamptz fecha_creacion
        timestamptz fecha_actualizacion
    }

    DOCUMENTO {
        bigint id PK
        bigint tramite_id FK
        enum tipo_documento "CEDULA|PASAPORTE|AUTORIZACION_NOTARIAL|OTRO"
        varchar numero_documento
        varchar url_archivo
        timestamptz fecha_carga
    }

    VEHICULO {
        bigint id PK
        bigint tramite_id FK UK
        varchar patente
        varchar marca
        varchar modelo
        int anio
        varchar pais_registro
        enum tipo_formulario "SALIDA_TEMPORAL|ADMISION_TEMPORAL"
    }

    DECLARACION_SAG {
        bigint id PK
        bigint tramite_id FK UK
        boolean transporta_productos
        text detalle_productos
        bigint funcionario_sag_id FK "nullable"
        enum resultado "PENDIENTE|APROBADA|OBSERVADA|RECHAZADA"
        timestamptz fecha_revision "nullable"
    }

    REVISION_PDI {
        bigint id PK
        bigint tramite_id FK UK
        bigint funcionario_pdi_id FK "nullable"
        enum estado_revision "PENDIENTE|APROBADA|OBSERVADA|RECHAZADA"
        text observaciones
        timestamptz fecha_revision "nullable"
    }

    VALIDACION_ADUANA_ARG {
        bigint id PK
        bigint tramite_id FK UK
        enum estado_validacion "PENDIENTE|VALIDADO|RECHAZADO|ERROR"
        varchar referencia_externa
        jsonb respuesta_externa
        int intentos
        timestamptz fecha_validacion "nullable"
    }

    NOTIFICACION {
        bigint id PK
        bigint tramite_id FK
        bigint usuario_destino_id FK
        enum canal "EMAIL|SMS"
        text mensaje
        enum estado_envio "PENDIENTE|ENVIADA|FALLIDA"
        timestamptz fecha_envio "nullable"
    }

    REPORTE_ESTADISTICO {
        bigint id PK
        bigint generado_por_id FK
        enum tipo_reporte "INGRESOS|EGRESOS|CONSOLIDADO"
        enum formato "PDF|EXCEL"
        jsonb parametros "filtros: fecha, paso, tipo"
        varchar url_archivo
        timestamptz fecha_generacion
    }

    HISTORIAL_ESTADO_TRAMITE {
        bigint id PK
        bigint tramite_id FK
        enum estado_anterior "nullable"
        enum estado_nuevo
        bigint funcionario_id FK "nullable"
        text comentario
        timestamptz fecha_cambio
    }
```

---

## 2. Cardinalidades clave

| Relación | Cardinalidad | Regla |
|---|---|---|
| Pasajero → Trámite | 1 : 0..* | Un pasajero puede tener muchos trámites en el tiempo. |
| Funcionario → Trámite (asignado) | 0..1 : 0..* | Un funcionario atiende varios trámites; un trámite puede no tener funcionario aún. |
| Trámite → Documento | 1 : 0..* | Composición. Se borran en cascada con el trámite. |
| Trámite → Vehículo | 1 : 0..1 | Solo si `tipo_tramite = VEHICULO`. |
| Trámite → Declaración SAG | 1 : 0..1 | Solo si `tipo_tramite = SAG` (o si transporta productos). |
| Trámite → Revisión PDI | 1 : 0..1 | Todo trámite es fiscalizado por PDI. |
| Trámite → Validación Aduana Arg. | 1 : 0..1 | Validación externa; puede quedar `PENDIENTE`/`ERROR` por caída de API. |
| Trámite → Notificación | 1 : 0..* | Patrón Observer: cada cambio de estado dispara N notificaciones. |
| Trámite → Historial | 1 : 0..* | Auditoría append-only. |

---

## 3. DDL PostgreSQL

```sql
-- ============ ENUMS ============
CREATE TYPE tipo_usuario      AS ENUM ('PASAJERO','FUNCIONARIO','ADMINISTRADOR');
CREATE TYPE rol_funcionario   AS ENUM ('ADUANAS','PDI','SAG');
CREATE TYPE estado_cuenta     AS ENUM ('ACTIVA','DESHABILITADA');
CREATE TYPE tipo_tramite      AS ENUM ('MENORES','VEHICULO','SAG');
CREATE TYPE estado_tramite    AS ENUM ('BORRADOR','PENDIENTE','EN_REVISION','PENDIENTE_VALIDACION_EXTERNA','OBSERVADO','APROBADO','RECHAZADO');
CREATE TYPE tipo_documento    AS ENUM ('CEDULA','PASAPORTE','AUTORIZACION_NOTARIAL','OTRO');
CREATE TYPE tipo_formulario   AS ENUM ('SALIDA_TEMPORAL','ADMISION_TEMPORAL');
CREATE TYPE resultado_revision AS ENUM ('PENDIENTE','APROBADA','OBSERVADA','RECHAZADA');
CREATE TYPE estado_validacion AS ENUM ('PENDIENTE','VALIDADO','RECHAZADO','ERROR');
CREATE TYPE canal_notificacion AS ENUM ('EMAIL','SMS');
CREATE TYPE estado_envio       AS ENUM ('PENDIENTE','ENVIADA','FALLIDA');
CREATE TYPE tipo_reporte       AS ENUM ('INGRESOS','EGRESOS','CONSOLIDADO');
CREATE TYPE formato_reporte    AS ENUM ('PDF','EXCEL');

-- ============ USUARIO (herencia single-table) ============
CREATE TABLE usuario (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rut               VARCHAR(12)  NOT NULL UNIQUE,
    nombre            VARCHAR(150) NOT NULL,
    correo            VARCHAR(150) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    tipo_usuario      tipo_usuario NOT NULL,
    rol_funcionario   rol_funcionario,                  -- NULL salvo FUNCIONARIO
    estado_cuenta     estado_cuenta NOT NULL DEFAULT 'ACTIVA',
    dos_fa_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_rol_solo_funcionario
        CHECK (tipo_usuario = 'FUNCIONARIO' OR rol_funcionario IS NULL)
);

-- ============ TRAMITE ADUANERO (entidad central) ============
CREATE TABLE tramite_aduanero (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    folio                   VARCHAR(20) NOT NULL UNIQUE,
    pasajero_id             BIGINT NOT NULL REFERENCES usuario(id),
    funcionario_asignado_id BIGINT REFERENCES usuario(id),
    tipo_tramite            tipo_tramite   NOT NULL,
    estado                  estado_tramite NOT NULL DEFAULT 'BORRADOR',
    url_comprobante         VARCHAR(500),                       -- se llena al pasar a APROBADO
    fecha_creacion          TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_actualizacion     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tramite_pasajero ON tramite_aduanero(pasajero_id);
CREATE INDEX idx_tramite_estado   ON tramite_aduanero(estado);

-- ============ DOCUMENTO (1..* por trámite) ============
CREATE TABLE documento (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id      BIGINT NOT NULL REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    tipo_documento  tipo_documento NOT NULL,
    numero_documento VARCHAR(50),
    url_archivo     VARCHAR(500),
    fecha_carga     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_documento_tramite ON documento(tramite_id);

-- ============ VEHICULO (0..1 por trámite) ============
CREATE TABLE vehiculo (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id     BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    patente        VARCHAR(10) NOT NULL,
    marca          VARCHAR(60),
    modelo         VARCHAR(60),
    anio           INT,
    pais_registro  VARCHAR(40),
    tipo_formulario tipo_formulario NOT NULL
);

-- ============ DECLARACION SAG (0..1 por trámite) ============
CREATE TABLE declaracion_sag (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id          BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    transporta_productos BOOLEAN NOT NULL DEFAULT FALSE,
    detalle_productos   TEXT,
    funcionario_sag_id  BIGINT REFERENCES usuario(id),
    resultado           resultado_revision NOT NULL DEFAULT 'PENDIENTE',
    fecha_revision      TIMESTAMPTZ
);

-- ============ REVISION PDI (0..1 por trámite) ============
CREATE TABLE revision_pdi (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id        BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    funcionario_pdi_id BIGINT REFERENCES usuario(id),
    estado_revision   resultado_revision NOT NULL DEFAULT 'PENDIENTE',
    observaciones     TEXT,
    fecha_revision    TIMESTAMPTZ
);

-- ============ VALIDACION ADUANA ARGENTINA (0..1 por trámite) ============
CREATE TABLE validacion_aduana_arg (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id        BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    estado_validacion estado_validacion NOT NULL DEFAULT 'PENDIENTE',
    referencia_externa VARCHAR(100),
    respuesta_externa JSONB,
    intentos          INT NOT NULL DEFAULT 0,
    fecha_validacion  TIMESTAMPTZ
);

-- ============ NOTIFICACION (0..* por trámite) ============
CREATE TABLE notificacion (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id         BIGINT NOT NULL REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    usuario_destino_id BIGINT NOT NULL REFERENCES usuario(id),
    canal              canal_notificacion NOT NULL,
    mensaje            TEXT NOT NULL,
    estado_envio       estado_envio NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio        TIMESTAMPTZ
);
CREATE INDEX idx_notif_tramite ON notificacion(tramite_id);

-- ============ HISTORIAL DE ESTADOS (auditoría append-only) ============
CREATE TABLE historial_estado_tramite (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id      BIGINT NOT NULL REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    estado_anterior estado_tramite,
    estado_nuevo    estado_tramite NOT NULL,
    funcionario_id  BIGINT REFERENCES usuario(id),
    comentario      TEXT,
    fecha_cambio    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_hist_tramite ON historial_estado_tramite(tramite_id);

-- ============ REPORTE ESTADISTICO ============
CREATE TABLE reporte_estadistico (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    generado_por_id BIGINT NOT NULL REFERENCES usuario(id),
    tipo_reporte    tipo_reporte NOT NULL,
    formato         formato_reporte NOT NULL,
    parametros      JSONB,
    url_archivo     VARCHAR(500),
    fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

---

## 4. Mapeo a backend

**Si vas Spring Boot / JPA:**
- `usuario` → `@Entity` con `@Inheritance(strategy = SINGLE_TABLE)` + `@DiscriminatorColumn(name="tipo_usuario")`, o más simple: una sola entidad `Usuario` con campo `tipoUsuario` y `rolFuncionario`. Para un MVP de 6 días, la entidad única es más rápida y suficiente.
- Enums PostgreSQL → en JPA usa `@Enumerated(EnumType.STRING)` con `columnDefinition` o registra un `@Type` de Hibernate para enums nativos. Para ir rápido: define las columnas como `VARCHAR` con `CHECK` en vez de tipos `ENUM` nativos y mapea con `EnumType.STRING`. Menos fricción.
- Relaciones 0..1 (`vehiculo`, `declaracion_sag`, `revision_pdi`, `validacion_aduana_arg`) → `@OneToOne(mappedBy="tramite")`.
- Composición (`documento`, `notificacion`, `historial`) → `@OneToMany(cascade=ALL, orphanRemoval=true)`.

**Si vas Node / Prisma:**
- El DDL es casi 1:1 con `schema.prisma`. Los enums PostgreSQL mapean directo a `enum` de Prisma. Las relaciones 0..1 son `?` en el campo de relación.

**Login mínimo viable (lo que la profe va a tocar primero):**
- `usuario(correo, password_hash, tipo_usuario)` ya cubre auth. JWT con email+password. No montes roles granulares; `tipo_usuario` basta para el demo.

---

## 5. Lo que NO está en el ER (decisiones conscientes de scope)

- **Tablas catálogo** para tipos/estados: se modelaron como `ENUM` en vez de tablas, porque para un MVP académico no necesitas administrar catálogos en runtime. Si la profe pregunta "¿y si cambian los estados?", la respuesta es: enum → migración, costo bajo, scope controlado.
- **Integración real con APIs externas** (SAG/PDI/Aduana Argentina): el ER guarda los *resultados* de validación (`validacion_aduana_arg`, `revision_pdi`, `declaracion_sag`), pero la llamada HTTP real está fuera del esquema. En el demo, esos estados se setean manualmente por el funcionario o con un mock.
- **2FA**: hay un flag `dos_fa_habilitado` pero la implementación TOTP completa es opcional. No la necesitas para aprobar.