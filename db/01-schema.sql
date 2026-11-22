-- =====================================================================
--  Sistema de Actas - Esquema de base de datos
--  Compatible con MySQL 8.x (InnoDB, utf8mb4) y H2 (modo MySQL, tests).
--
--  Cambios respecto al esquema original de 2009 (MyISAM / latin1):
--    * InnoDB con claves foráneas reales e integridad referencial.
--    * HORA_INICIO / HORA_FIN pasan de VARCHAR ('9h00') a TIME.
--    * ESTADO restringido a 'programada' | 'cerrada'.
--    * Contraseñas almacenadas como hash PBKDF2 (CONTRASENA_HASH).
--    * Corrección de la columna REUNIONES_COD_REUNIONE -> REUNIONES_COD_REUNION.
--    * Restricciones UNIQUE para evitar temas o asistencias duplicadas.
--    * Se elimina la columna IP (siempre se guardaba 127.0.0.1) y la tabla
--      miembros_periodos, que la aplicación nunca utilizó.
-- =====================================================================

SET NAMES utf8mb4;

CREATE TABLE tipo_usuario (
    COD_TIPO_USUARIO INT AUTO_INCREMENT PRIMARY KEY,
    TIPO             VARCHAR(75) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    COD_USUARIO                   INT AUTO_INCREMENT PRIMARY KEY,
    USUARIO                       VARCHAR(75)  NOT NULL UNIQUE,
    CONTRASENA_HASH               VARCHAR(255) NOT NULL,
    CEDULA                        VARCHAR(10),
    TIPO_USUARIO_COD_TIPO_USUARIO INT NOT NULL,
    CONSTRAINT fk_usuario_tipo FOREIGN KEY (TIPO_USUARIO_COD_TIPO_USUARIO)
        REFERENCES tipo_usuario (COD_TIPO_USUARIO)
);

CREATE TABLE periodos (
    COD_PERIODO INT AUTO_INCREMENT PRIMARY KEY,
    INICIO      DATE    NOT NULL,
    FIN         DATE    NOT NULL,
    ES_ACTUAL   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE cargos (
    COD_CARGO INT AUTO_INCREMENT PRIMARY KEY,
    CARGO     VARCHAR(75) NOT NULL UNIQUE
);

CREATE TABLE comites (
    COD_COMITE           INT AUTO_INCREMENT PRIMARY KEY,
    COMITE               VARCHAR(75) NOT NULL,
    PERIODOS_COD_PERIODO INT,
    CONSTRAINT fk_comites_periodo FOREIGN KEY (PERIODOS_COD_PERIODO)
        REFERENCES periodos (COD_PERIODO)
);

CREATE TABLE miembros (
    COD_MIEMBRO      INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE           VARCHAR(100) NOT NULL,
    CEDULA           VARCHAR(10),
    TELEFONO         VARCHAR(20),
    CELULAR          VARCHAR(20),
    CORREO           VARCHAR(100),
    CARGOS_COD_CARGO INT NOT NULL,
    CONSTRAINT fk_miembros_cargo FOREIGN KEY (CARGOS_COD_CARGO)
        REFERENCES cargos (COD_CARGO)
);

CREATE TABLE miembros_comite (
    COD_MC               INT AUTO_INCREMENT PRIMARY KEY,
    COMITES_COD_COMITE   INT NOT NULL,
    MIEMBROS_COD_MIEMBRO INT NOT NULL,
    CONSTRAINT uq_miembro_comite UNIQUE (COMITES_COD_COMITE, MIEMBROS_COD_MIEMBRO),
    CONSTRAINT fk_mc_comite FOREIGN KEY (COMITES_COD_COMITE)
        REFERENCES comites (COD_COMITE),
    CONSTRAINT fk_mc_miembro FOREIGN KEY (MIEMBROS_COD_MIEMBRO)
        REFERENCES miembros (COD_MIEMBRO) ON DELETE CASCADE
);

CREATE TABLE tipo_reunion (
    COD_TIPO_REUNION INT AUTO_INCREMENT PRIMARY KEY,
    TIPO             VARCHAR(70) NOT NULL UNIQUE
);

CREATE TABLE tipo_resolucion (
    COD_TIPO_RESOLUCION INT AUTO_INCREMENT PRIMARY KEY,
    TIPO                VARCHAR(75) NOT NULL UNIQUE
);

CREATE TABLE reuniones (
    COD_REUNION                   INT AUTO_INCREMENT PRIMARY KEY,
    MOTIVO                        VARCHAR(200) NOT NULL,
    FECHA                         DATE         NOT NULL,
    HORA_INICIO                   TIME         NOT NULL,
    HORA_FIN                      TIME,
    LUGAR                         VARCHAR(150),
    ESTADO                        VARCHAR(20)  NOT NULL DEFAULT 'programada',
    PERIODOS_COD_PERIODO          INT,
    TIPO_REUNION_COD_TIPO_REUNION INT NOT NULL,
    USUARIO_COD_USUARIO           INT,
    COD_COMITE                    INT NOT NULL,
    CONSTRAINT ck_reunion_estado CHECK (ESTADO IN ('programada', 'cerrada')),
    CONSTRAINT fk_reunion_periodo FOREIGN KEY (PERIODOS_COD_PERIODO)
        REFERENCES periodos (COD_PERIODO),
    CONSTRAINT fk_reunion_tipo FOREIGN KEY (TIPO_REUNION_COD_TIPO_REUNION)
        REFERENCES tipo_reunion (COD_TIPO_REUNION),
    CONSTRAINT fk_reunion_usuario FOREIGN KEY (USUARIO_COD_USUARIO)
        REFERENCES usuario (COD_USUARIO) ON DELETE SET NULL,
    CONSTRAINT fk_reunion_comite FOREIGN KEY (COD_COMITE)
        REFERENCES comites (COD_COMITE)
);

CREATE TABLE temas (
    COD_TEMA              INT AUTO_INCREMENT PRIMARY KEY,
    NUMERO                INT          NOT NULL,
    TEMA                  VARCHAR(200) NOT NULL,
    REUNIONES_COD_REUNION INT          NOT NULL,
    CONSTRAINT uq_tema_numero UNIQUE (REUNIONES_COD_REUNION, NUMERO),
    CONSTRAINT fk_tema_reunion FOREIGN KEY (REUNIONES_COD_REUNION)
        REFERENCES reuniones (COD_REUNION) ON DELETE CASCADE
);

CREATE TABLE intervenciones (
    COD_INTERVENCION      INT AUTO_INCREMENT PRIMARY KEY,
    INTERVENCION          VARCHAR(1000) NOT NULL,
    REUNIONES_COD_REUNION INT NOT NULL,
    MIEMBROS_COD_MIEMBRO  INT NOT NULL,
    TEMAS_COD_TEMA        INT NOT NULL,
    CONSTRAINT fk_int_reunion FOREIGN KEY (REUNIONES_COD_REUNION)
        REFERENCES reuniones (COD_REUNION) ON DELETE CASCADE,
    CONSTRAINT fk_int_miembro FOREIGN KEY (MIEMBROS_COD_MIEMBRO)
        REFERENCES miembros (COD_MIEMBRO),
    CONSTRAINT fk_int_tema FOREIGN KEY (TEMAS_COD_TEMA)
        REFERENCES temas (COD_TEMA) ON DELETE CASCADE
);

CREATE TABLE resoluciones (
    COD_RESOLUCION                      INT AUTO_INCREMENT PRIMARY KEY,
    RESOLUCION                          VARCHAR(1000) NOT NULL,
    TIPO_RESOLUCION_COD_TIPO_RESOLUCION INT NOT NULL,
    REUNIONES_COD_REUNION               INT NOT NULL,
    TEMAS_COD_TEMA                      INT NOT NULL,
    CONSTRAINT fk_res_tipo FOREIGN KEY (TIPO_RESOLUCION_COD_TIPO_RESOLUCION)
        REFERENCES tipo_resolucion (COD_TIPO_RESOLUCION),
    CONSTRAINT fk_res_reunion FOREIGN KEY (REUNIONES_COD_REUNION)
        REFERENCES reuniones (COD_REUNION) ON DELETE CASCADE,
    CONSTRAINT fk_res_tema FOREIGN KEY (TEMAS_COD_TEMA)
        REFERENCES temas (COD_TEMA) ON DELETE CASCADE
);

CREATE TABLE asistencias (
    COD_ASISTENCIA        INT AUTO_INCREMENT PRIMARY KEY,
    PERIODOS_COD_PERIODO  INT,
    MIEMBROS_COD_MIEMBRO  INT NOT NULL,
    COMITES_COD_COMITE    INT NOT NULL,
    REUNIONES_COD_REUNION INT NOT NULL,
    CONSTRAINT uq_asistencia UNIQUE (REUNIONES_COD_REUNION, MIEMBROS_COD_MIEMBRO),
    CONSTRAINT fk_asis_periodo FOREIGN KEY (PERIODOS_COD_PERIODO)
        REFERENCES periodos (COD_PERIODO),
    CONSTRAINT fk_asis_miembro FOREIGN KEY (MIEMBROS_COD_MIEMBRO)
        REFERENCES miembros (COD_MIEMBRO),
    CONSTRAINT fk_asis_comite FOREIGN KEY (COMITES_COD_COMITE)
        REFERENCES comites (COD_COMITE),
    CONSTRAINT fk_asis_reunion FOREIGN KEY (REUNIONES_COD_REUNION)
        REFERENCES reuniones (COD_REUNION) ON DELETE CASCADE
);
