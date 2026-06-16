-- =====================================================================
-- V1__init_schema.sql  —  Esquema inicial de SIGA
-- Sistema Integrado de Gestión Aduanera (Paso Los Libertadores Chile-Argentina)
-- Fuente de verdad: docs/sigaMER.md
--
-- Adaptaciones respecto del DDL del MER (documentadas en CLAUDE.md):
--   * ENUM nativos de PostgreSQL  -> VARCHAR + CHECK  (recomendado por el propio MER
--     para reducir fricción con JPA / @Enumerated(STRING)).
--   * JSONB                       -> TEXT  (JSON serializado como String en el MVP).
--   * usuario.rut y usuario.password_hash -> NULLABLE  (el registro vía Google OAuth
--     no provee RUT ni contraseña local; se completan después).
-- El resto (nombres de tablas/columnas, FKs, cardinalidades, índices) respeta el MER.
-- =====================================================================

-- ============ USUARIO (tabla única con discriminador tipo_usuario) ============
CREATE TABLE usuario (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rut               VARCHAR(12) UNIQUE,
    nombre            VARCHAR(150) NOT NULL,
    correo            VARCHAR(150) NOT NULL UNIQUE,
    password_hash     VARCHAR(255),
    tipo_usuario      VARCHAR(20)  NOT NULL,
    rol_funcionario   VARCHAR(20),
    estado_cuenta     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVA',
    dos_fa_habilitado BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_tipo_usuario      CHECK (tipo_usuario IN ('PASAJERO','FUNCIONARIO','ADMINISTRADOR')),
    CONSTRAINT chk_rol_funcionario   CHECK (rol_funcionario IS NULL OR rol_funcionario IN ('ADUANAS','PDI','SAG')),
    CONSTRAINT chk_estado_cuenta     CHECK (estado_cuenta IN ('ACTIVA','DESHABILITADA')),
    CONSTRAINT chk_rol_solo_funcionario CHECK (tipo_usuario = 'FUNCIONARIO' OR rol_funcionario IS NULL)
);

-- ============ TRAMITE ADUANERO (entidad central) ============
CREATE TABLE tramite_aduanero (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    folio                   VARCHAR(20) NOT NULL UNIQUE,
    pasajero_id             BIGINT NOT NULL REFERENCES usuario(id),
    funcionario_asignado_id BIGINT REFERENCES usuario(id),
    tipo_tramite            VARCHAR(20) NOT NULL,
    estado                  VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    url_comprobante         VARCHAR(500),
    fecha_creacion          TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_actualizacion     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_tipo_tramite CHECK (tipo_tramite IN ('MENORES','VEHICULO','SAG')),
    CONSTRAINT chk_estado_tramite CHECK (estado IN
        ('BORRADOR','PENDIENTE','EN_REVISION','PENDIENTE_VALIDACION_EXTERNA','OBSERVADO','APROBADO','RECHAZADO'))
);
CREATE INDEX idx_tramite_pasajero ON tramite_aduanero(pasajero_id);
CREATE INDEX idx_tramite_estado   ON tramite_aduanero(estado);

-- ============ DOCUMENTO (1..* por trámite) ============
CREATE TABLE documento (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id       BIGINT NOT NULL REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    tipo_documento   VARCHAR(25) NOT NULL,
    numero_documento VARCHAR(50),
    url_archivo      VARCHAR(500),
    fecha_carga      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_tipo_documento CHECK (tipo_documento IN ('CEDULA','PASAPORTE','AUTORIZACION_NOTARIAL','OTRO'))
);
CREATE INDEX idx_documento_tramite ON documento(tramite_id);

-- ============ VEHICULO (0..1 por trámite) ============
CREATE TABLE vehiculo (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id      BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    patente         VARCHAR(10) NOT NULL,
    marca           VARCHAR(60),
    modelo          VARCHAR(60),
    anio            INT,
    pais_registro   VARCHAR(40),
    tipo_formulario VARCHAR(20) NOT NULL,
    CONSTRAINT chk_tipo_formulario CHECK (tipo_formulario IN ('SALIDA_TEMPORAL','ADMISION_TEMPORAL'))
);

-- ============ DECLARACION SAG (0..1 por trámite) ============
CREATE TABLE declaracion_sag (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id           BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    transporta_productos BOOLEAN NOT NULL DEFAULT FALSE,
    detalle_productos    TEXT,
    funcionario_sag_id   BIGINT REFERENCES usuario(id),
    resultado            VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    fecha_revision       TIMESTAMPTZ,
    CONSTRAINT chk_resultado_sag CHECK (resultado IN ('PENDIENTE','APROBADA','OBSERVADA','RECHAZADA'))
);

-- ============ REVISION PDI (0..1 por trámite) ============
CREATE TABLE revision_pdi (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id         BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    funcionario_pdi_id BIGINT REFERENCES usuario(id),
    estado_revision    VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    observaciones      TEXT,
    fecha_revision     TIMESTAMPTZ,
    CONSTRAINT chk_estado_revision_pdi CHECK (estado_revision IN ('PENDIENTE','APROBADA','OBSERVADA','RECHAZADA'))
);

-- ============ VALIDACION ADUANA ARGENTINA (0..1 por trámite) ============
CREATE TABLE validacion_aduana_arg (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id         BIGINT NOT NULL UNIQUE REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    estado_validacion  VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    referencia_externa VARCHAR(100),
    respuesta_externa  TEXT,
    intentos           INT NOT NULL DEFAULT 0,
    fecha_validacion   TIMESTAMPTZ,
    CONSTRAINT chk_estado_validacion CHECK (estado_validacion IN ('PENDIENTE','VALIDADO','RECHAZADO','ERROR'))
);

-- ============ NOTIFICACION (0..* por trámite) ============
CREATE TABLE notificacion (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id         BIGINT NOT NULL REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    usuario_destino_id BIGINT NOT NULL REFERENCES usuario(id),
    canal              VARCHAR(10) NOT NULL,
    mensaje            TEXT NOT NULL,
    estado_envio       VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio        TIMESTAMPTZ,
    CONSTRAINT chk_canal_notificacion CHECK (canal IN ('EMAIL','SMS')),
    CONSTRAINT chk_estado_envio       CHECK (estado_envio IN ('PENDIENTE','ENVIADA','FALLIDA'))
);
CREATE INDEX idx_notif_tramite ON notificacion(tramite_id);

-- ============ HISTORIAL DE ESTADOS (auditoría append-only) ============
CREATE TABLE historial_estado_tramite (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tramite_id      BIGINT NOT NULL REFERENCES tramite_aduanero(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30),
    estado_nuevo    VARCHAR(30) NOT NULL,
    funcionario_id  BIGINT REFERENCES usuario(id),
    comentario      TEXT,
    fecha_cambio    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_estado_anterior CHECK (estado_anterior IS NULL OR estado_anterior IN
        ('BORRADOR','PENDIENTE','EN_REVISION','PENDIENTE_VALIDACION_EXTERNA','OBSERVADO','APROBADO','RECHAZADO')),
    CONSTRAINT chk_estado_nuevo CHECK (estado_nuevo IN
        ('BORRADOR','PENDIENTE','EN_REVISION','PENDIENTE_VALIDACION_EXTERNA','OBSERVADO','APROBADO','RECHAZADO'))
);
CREATE INDEX idx_hist_tramite ON historial_estado_tramite(tramite_id);

-- ============ REPORTE ESTADISTICO ============
CREATE TABLE reporte_estadistico (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    generado_por_id  BIGINT NOT NULL REFERENCES usuario(id),
    tipo_reporte     VARCHAR(15) NOT NULL,
    formato          VARCHAR(10) NOT NULL,
    parametros       TEXT,
    url_archivo      VARCHAR(500),
    fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_tipo_reporte CHECK (tipo_reporte IN ('INGRESOS','EGRESOS','CONSOLIDADO')),
    CONSTRAINT chk_formato_reporte CHECK (formato IN ('PDF','EXCEL'))
);
