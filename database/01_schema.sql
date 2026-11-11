-- =====================================================================
-- Proyecto Final Bases de Datos I - Tours y Actividades
-- 01_schema.sql : creación de tablas, claves y restricciones
-- Motor: MySQL 8.x
--
-- Convenciones:
--  * Los valores de dominio se guardan en MAYÚSCULAS y coinciden con los
--    nombres de los enums de Java (@Enumerated(EnumType.STRING)).
--  * Las PK son BIGINT para mapear a Long en las entidades JPA.
--  * Las reglas que necesitan mirar otras tablas (cupo, idioma del guía,
--    cruce de horarios, edad mínima, prestador activo, etc.) se validan
--    en la capa de servicios, como indica la entrega.
--  * MySQL no permite CURDATE() dentro de un CHECK, por eso "fecha de
--    nacimiento no futura" y "salida no programada en el pasado" también
--    se validan en los servicios.
-- =====================================================================

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS tours_actividades
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE tours_actividades;

-- =====================================================================
-- NÚCLEO COMÚN
-- =====================================================================

CREATE TABLE municipio (
  id_municipio  BIGINT       NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(100) NOT NULL,
  codigo_dane   VARCHAR(5)   NOT NULL,
  CONSTRAINT pk_municipio PRIMARY KEY (id_municipio),
  CONSTRAINT uq_municipio_nombre UNIQUE (nombre),
  CONSTRAINT uq_municipio_dane UNIQUE (codigo_dane),
  CONSTRAINT ck_municipio_dane CHECK (codigo_dane REGEXP '^[0-9]{5}$')
);

CREATE TABLE tipo_prestador (
  id_tipo_prestador BIGINT      NOT NULL AUTO_INCREMENT,
  nombre_tipo       VARCHAR(60) NOT NULL,
  CONSTRAINT pk_tipo_prestador PRIMARY KEY (id_tipo_prestador),
  CONSTRAINT uq_tipo_prestador_nombre UNIQUE (nombre_tipo)
);

CREATE TABLE prestador (
  id_prestador      BIGINT       NOT NULL AUTO_INCREMENT,
  nombre_comercial  VARCHAR(120) NOT NULL,
  nit_documento     VARCHAR(20)  NOT NULL,
  numero_rnt        VARCHAR(30)  NOT NULL,
  direccion         VARCHAR(200) NOT NULL,
  telefono          VARCHAR(20)  NOT NULL,
  correo            VARCHAR(254) NOT NULL,
  estado            VARCHAR(15)  NOT NULL DEFAULT 'ACTIVO',
  id_municipio      BIGINT       NOT NULL,
  id_tipo_prestador BIGINT       NOT NULL,
  CONSTRAINT pk_prestador PRIMARY KEY (id_prestador),
  CONSTRAINT uq_prestador_nit UNIQUE (nit_documento),
  CONSTRAINT uq_prestador_rnt UNIQUE (numero_rnt),
  CONSTRAINT uq_prestador_correo UNIQUE (correo),
  CONSTRAINT ck_prestador_estado CHECK (estado IN ('ACTIVO','SUSPENDIDO','RETIRADO')),
  CONSTRAINT ck_prestador_telefono CHECK (telefono REGEXP '^3[0-9]{9}$'),
  CONSTRAINT ck_prestador_correo CHECK (correo REGEXP '^[^@[:space:]]+@[^@[:space:]]+\\.[^@[:space:]]+$'),
  CONSTRAINT ck_prestador_nombre CHECK (CHAR_LENGTH(TRIM(nombre_comercial)) > 0),
  CONSTRAINT fk_prestador_municipio FOREIGN KEY (id_municipio)
    REFERENCES municipio (id_municipio) ON DELETE RESTRICT,
  CONSTRAINT fk_prestador_tipo FOREIGN KEY (id_tipo_prestador)
    REFERENCES tipo_prestador (id_tipo_prestador) ON DELETE RESTRICT
);

CREATE TABLE cliente (
  id_cliente        BIGINT       NOT NULL AUTO_INCREMENT,
  nombre            VARCHAR(80)  NOT NULL,
  apellido          VARCHAR(80)  NOT NULL,
  tipo_documento    VARCHAR(20)  NOT NULL,
  numero_documento  VARCHAR(30)  NOT NULL,
  correo            VARCHAR(254) NOT NULL,
  telefono          VARCHAR(20)  NOT NULL,
  fecha_nacimiento  DATE         NOT NULL,
  ciudad_origen     VARCHAR(100) NOT NULL,
  CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
  CONSTRAINT uq_cliente_documento UNIQUE (tipo_documento, numero_documento),
  CONSTRAINT uq_cliente_correo UNIQUE (correo),
  CONSTRAINT ck_cliente_tipo_doc CHECK (tipo_documento IN
    ('CEDULA_CIUDADANIA','CEDULA_EXTRANJERIA','PASAPORTE','TARJETA_IDENTIDAD')),
  CONSTRAINT ck_cliente_telefono CHECK (telefono REGEXP '^3[0-9]{9}$'),
  CONSTRAINT ck_cliente_correo CHECK (correo REGEXP '^[^@[:space:]]+@[^@[:space:]]+\\.[^@[:space:]]+$')
);

CREATE TABLE perfil_cliente (
  id_cliente                  BIGINT       NOT NULL,
  correo_cuenta               VARCHAR(254) NOT NULL,
  contrasena_cifrada          VARCHAR(255) NOT NULL,
  idioma_preferencia          VARCHAR(20)  NOT NULL DEFAULT 'ESPANOL',
  canal_notificacion_preferido VARCHAR(20) NOT NULL DEFAULT 'CORREO',
  anticipacion_recordatorios  INT          NOT NULL DEFAULT 1,
  acepta_promociones          BOOLEAN      NOT NULL DEFAULT FALSE,
  CONSTRAINT pk_perfil_cliente PRIMARY KEY (id_cliente),
  CONSTRAINT uq_perfil_correo_cuenta UNIQUE (correo_cuenta),
  CONSTRAINT ck_perfil_idioma CHECK (idioma_preferencia IN ('ESPANOL','INGLES')),
  CONSTRAINT ck_perfil_canal CHECK (canal_notificacion_preferido IN ('CORREO','SMS','WHATSAPP')),
  CONSTRAINT ck_perfil_anticipacion CHECK (anticipacion_recordatorios IN (1,3,7)),
  CONSTRAINT fk_perfil_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente (id_cliente) ON DELETE CASCADE
);

CREATE TABLE plan (
  id_plan       BIGINT        NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(120)  NOT NULL,
  descripcion   TEXT          NOT NULL,
  tipo          VARCHAR(30)   NOT NULL,
  precio        DECIMAL(12,2) NOT NULL,
  fecha_inicio  DATE          NOT NULL,
  fecha_fin     DATE          NOT NULL,
  estado        VARCHAR(15)   NOT NULL DEFAULT 'ACTIVO',
  id_prestador  BIGINT        NOT NULL,
  CONSTRAINT pk_plan PRIMARY KEY (id_plan),
  CONSTRAINT uq_plan_prestador_nombre UNIQUE (id_prestador, nombre),
  CONSTRAINT ck_plan_tipo CHECK (tipo IN ('BASICO','ESTANDAR','PREMIUM','TODO_INCLUIDO')),
  CONSTRAINT ck_plan_estado CHECK (estado IN ('ACTIVO','INACTIVO')),
  CONSTRAINT ck_plan_precio CHECK (precio > 0),
  CONSTRAINT ck_plan_fechas CHECK (fecha_inicio < fecha_fin),
  CONSTRAINT fk_plan_prestador FOREIGN KEY (id_prestador)
    REFERENCES prestador (id_prestador) ON DELETE RESTRICT
);

CREATE TABLE rol (
  id_rol  BIGINT      NOT NULL AUTO_INCREMENT,
  nombre  VARCHAR(40) NOT NULL,
  CONSTRAINT pk_rol PRIMARY KEY (id_rol),
  CONSTRAINT uq_rol_nombre UNIQUE (nombre)
);

CREATE TABLE usuario_sistema (
  id_usuario          BIGINT       NOT NULL AUTO_INCREMENT,
  nombre_usuario      VARCHAR(80)  NOT NULL,
  contrasena_cifrada  VARCHAR(255) NOT NULL,
  estado              VARCHAR(15)  NOT NULL DEFAULT 'ACTIVO',
  id_rol              BIGINT       NOT NULL,
  id_prestador        BIGINT       NULL,
  CONSTRAINT pk_usuario_sistema PRIMARY KEY (id_usuario),
  CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario),
  CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO','BLOQUEADO')),
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol)
    REFERENCES rol (id_rol) ON DELETE RESTRICT,
  CONSTRAINT fk_usuario_prestador FOREIGN KEY (id_prestador)
    REFERENCES prestador (id_prestador) ON DELETE RESTRICT
);

-- =====================================================================
-- MÓDULO TOURS Y ACTIVIDADES
-- =====================================================================

CREATE TABLE categoria_tour (
  id_categoria BIGINT      NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(80) NOT NULL,
  CONSTRAINT pk_categoria_tour PRIMARY KEY (id_categoria),
  CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);

CREATE TABLE tour (
  id_tour         BIGINT        NOT NULL AUTO_INCREMENT,
  nombre          VARCHAR(120)  NOT NULL,
  descripcion     TEXT          NOT NULL,
  duracion_horas  DECIMAL(5,2)  NOT NULL,
  dificultad      VARCHAR(20)   NOT NULL,
  edad_minima     INT           NOT NULL DEFAULT 0,
  precio_persona  DECIMAL(12,2) NOT NULL,
  id_categoria    BIGINT        NOT NULL,
  id_prestador    BIGINT        NOT NULL,
  CONSTRAINT pk_tour PRIMARY KEY (id_tour),
  CONSTRAINT uq_tour_prestador_nombre UNIQUE (id_prestador, nombre),
  CONSTRAINT ck_tour_duracion CHECK (duracion_horas > 0),
  CONSTRAINT ck_tour_precio CHECK (precio_persona > 0),
  CONSTRAINT ck_tour_edad CHECK (edad_minima >= 0),
  CONSTRAINT ck_tour_dificultad CHECK (dificultad IN ('BAJA','MEDIA','ALTA')),
  CONSTRAINT fk_tour_categoria FOREIGN KEY (id_categoria)
    REFERENCES categoria_tour (id_categoria) ON DELETE RESTRICT,
  CONSTRAINT fk_tour_prestador FOREIGN KEY (id_prestador)
    REFERENCES prestador (id_prestador) ON DELETE RESTRICT
);

CREATE TABLE guia (
  id_guia             BIGINT       NOT NULL AUTO_INCREMENT,
  nombre              VARCHAR(120) NOT NULL,
  documento           VARCHAR(30)  NOT NULL,
  tarjeta_profesional VARCHAR(40)  NOT NULL,
  CONSTRAINT pk_guia PRIMARY KEY (id_guia),
  CONSTRAINT uq_guia_documento UNIQUE (documento),
  CONSTRAINT uq_guia_tarjeta UNIQUE (tarjeta_profesional)
);

CREATE TABLE idioma (
  id_idioma      BIGINT      NOT NULL AUTO_INCREMENT,
  nombre_idioma  VARCHAR(40) NOT NULL,
  CONSTRAINT pk_idioma PRIMARY KEY (id_idioma),
  CONSTRAINT uq_idioma_nombre UNIQUE (nombre_idioma)
);

CREATE TABLE guia_idioma (
  id_guia    BIGINT NOT NULL,
  id_idioma  BIGINT NOT NULL,
  CONSTRAINT pk_guia_idioma PRIMARY KEY (id_guia, id_idioma),
  CONSTRAINT fk_guia_idioma_guia FOREIGN KEY (id_guia)
    REFERENCES guia (id_guia) ON DELETE CASCADE,
  CONSTRAINT fk_guia_idioma_idioma FOREIGN KEY (id_idioma)
    REFERENCES idioma (id_idioma) ON DELETE RESTRICT
);

CREATE TABLE salida_tour (
  id_salida     BIGINT      NOT NULL AUTO_INCREMENT,
  fecha_salida  DATE        NOT NULL,
  hora_salida   TIME        NOT NULL,
  cupo          INT         NOT NULL,
  estado        VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
  id_tour       BIGINT      NOT NULL,
  id_guia       BIGINT      NOT NULL,
  id_idioma     BIGINT      NOT NULL,
  CONSTRAINT pk_salida_tour PRIMARY KEY (id_salida),
  CONSTRAINT uq_salida_guia_horario UNIQUE (id_guia, fecha_salida, hora_salida),
  CONSTRAINT ck_salida_cupo CHECK (cupo > 0),
  CONSTRAINT ck_salida_estado CHECK (estado IN ('PROGRAMADA','CANCELADA','REALIZADA')),
  CONSTRAINT fk_salida_tour FOREIGN KEY (id_tour)
    REFERENCES tour (id_tour) ON DELETE RESTRICT,
  CONSTRAINT fk_salida_guia FOREIGN KEY (id_guia)
    REFERENCES guia (id_guia) ON DELETE RESTRICT,
  CONSTRAINT fk_salida_idioma FOREIGN KEY (id_idioma)
    REFERENCES idioma (id_idioma) ON DELETE RESTRICT
);

-- =====================================================================
-- RESERVAS, PAGOS Y NOTIFICACIONES
-- =====================================================================

CREATE TABLE reserva (
  id_reserva         BIGINT        NOT NULL AUTO_INCREMENT,
  fecha_creacion     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_servicio     DATE          NOT NULL,
  estado             VARCHAR(15)   NOT NULL DEFAULT 'PENDIENTE',
  valor_total        DECIMAL(12,2) NOT NULL,
  fecha_cancelacion  DATETIME      NULL,
  canal              VARCHAR(25)   NOT NULL,
  id_cliente         BIGINT        NOT NULL,
  id_plan            BIGINT        NOT NULL,
  id_salida          BIGINT        NOT NULL,
  CONSTRAINT pk_reserva PRIMARY KEY (id_reserva),
  CONSTRAINT ck_reserva_estado CHECK (estado IN ('PENDIENTE','CONFIRMADA','CANCELADA','COMPLETADA')),
  CONSTRAINT ck_reserva_canal CHECK (canal IN
    ('WEB','APP_MOVIL','TELEFONO','MOSTRADOR','AGENCIA_INTERMEDIARIA')),
  CONSTRAINT ck_reserva_valor CHECK (valor_total > 0),
  CONSTRAINT ck_reserva_fecha_servicio CHECK (fecha_servicio >= DATE(fecha_creacion)),
  CONSTRAINT ck_reserva_cancelacion CHECK (
    (estado = 'CANCELADA' AND fecha_cancelacion IS NOT NULL AND fecha_cancelacion >= fecha_creacion)
    OR (estado <> 'CANCELADA' AND fecha_cancelacion IS NULL)),
  CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente (id_cliente) ON DELETE RESTRICT,
  CONSTRAINT fk_reserva_plan FOREIGN KEY (id_plan)
    REFERENCES plan (id_plan) ON DELETE RESTRICT,
  CONSTRAINT fk_reserva_salida FOREIGN KEY (id_salida)
    REFERENCES salida_tour (id_salida) ON DELETE RESTRICT
);

CREATE TABLE participante (
  id_reserva        BIGINT       NOT NULL,
  num_participante  INT          NOT NULL,
  nombre            VARCHAR(120) NOT NULL,
  documento         VARCHAR(30)  NOT NULL,
  fecha_nacimiento  DATE         NOT NULL,
  CONSTRAINT pk_participante PRIMARY KEY (id_reserva, num_participante),
  CONSTRAINT uq_participante_reserva_doc UNIQUE (id_reserva, documento),
  CONSTRAINT ck_participante_num CHECK (num_participante > 0),
  CONSTRAINT fk_participante_reserva FOREIGN KEY (id_reserva)
    REFERENCES reserva (id_reserva) ON DELETE CASCADE
);

CREATE TABLE pago (
  id_pago                 BIGINT        NOT NULL AUTO_INCREMENT,
  fecha                   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  monto                   DECIMAL(12,2) NOT NULL,
  metodo                  VARCHAR(30)   NOT NULL,
  estado                  VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
  referencia_transaccion  VARCHAR(100)  NULL,
  id_reserva              BIGINT        NOT NULL,
  CONSTRAINT pk_pago PRIMARY KEY (id_pago),
  CONSTRAINT uq_pago_referencia UNIQUE (referencia_transaccion),
  CONSTRAINT ck_pago_monto CHECK (monto > 0),
  CONSTRAINT ck_pago_metodo CHECK (metodo IN
    ('TARJETA_CREDITO','TARJETA_DEBITO','PSE','TRANSFERENCIA','EFECTIVO')),
  CONSTRAINT ck_pago_estado CHECK (estado IN ('EXITOSO','FALLIDO','PENDIENTE','REEMBOLSADO')),
  CONSTRAINT ck_pago_referencia CHECK (
    metodo NOT IN ('TARJETA_CREDITO','TARJETA_DEBITO','PSE')
    OR referencia_transaccion IS NOT NULL),
  CONSTRAINT fk_pago_reserva FOREIGN KEY (id_reserva)
    REFERENCES reserva (id_reserva) ON DELETE RESTRICT
);

CREATE TABLE notificacion (
  id_notificacion   BIGINT      NOT NULL AUTO_INCREMENT,
  tipo              VARCHAR(40) NOT NULL,
  canal             VARCHAR(20) NOT NULL,
  mensaje           TEXT        NOT NULL,
  fecha_programada  DATETIME    NOT NULL,
  fecha_envio       DATETIME    NULL,
  estado            VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
  id_cliente        BIGINT      NOT NULL,
  id_reserva        BIGINT      NULL,
  CONSTRAINT pk_notificacion PRIMARY KEY (id_notificacion),
  CONSTRAINT ck_notificacion_tipo CHECK (tipo IN
    ('RECORDATORIO_RESERVA','CONFIRMACION_RESERVA','CONFIRMACION_PAGO','CAMBIO_ESTADO','PROMOCION')),
  CONSTRAINT ck_notificacion_canal CHECK (canal IN ('CORREO','SMS','WHATSAPP')),
  CONSTRAINT ck_notificacion_estado CHECK (estado IN ('PENDIENTE','ENVIADA','FALLIDA','LEIDA')),
  CONSTRAINT ck_notificacion_envio CHECK (fecha_envio IS NULL OR fecha_envio >= fecha_programada),
  -- La regla "promoción sin reserva / recordatorio con reserva" se valida en
  -- NotificacionService: MySQL no permite un CHECK sobre id_reserva porque
  -- su FK tiene ON DELETE SET NULL.
  CONSTRAINT fk_notificacion_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente (id_cliente) ON DELETE CASCADE,
  CONSTRAINT fk_notificacion_reserva FOREIGN KEY (id_reserva)
    REFERENCES reserva (id_reserva) ON DELETE SET NULL
);

-- =====================================================================
-- ÍNDICES (las FK y los UNIQUE ya crean su índice automáticamente)
-- =====================================================================
CREATE INDEX ix_reserva_fecha_servicio   ON reserva (fecha_servicio);
CREATE INDEX ix_reserva_estado           ON reserva (estado);
CREATE INDEX ix_pago_fecha               ON pago (fecha);
CREATE INDEX ix_notificacion_pendientes  ON notificacion (estado, fecha_programada);
CREATE INDEX ix_salida_fecha             ON salida_tour (fecha_salida);
CREATE INDEX ix_participante_documento   ON participante (documento);
