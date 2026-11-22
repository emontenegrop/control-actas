-- =====================================================================
--  Datos de demostración (100 % FICTICIOS).
--  Nombres, cédulas, teléfonos y correos son inventados; las cédulas solo
--  cumplen el algoritmo de verificación (módulo 10) para fines de prueba.
--
--  Usuario de demostración:  admin / admin123
--
--  Los IDs se asignan en el orden de inserción (1, 2, 3...) sobre una base
--  vacía, por eso las claves foráneas usan esos valores directamente.
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO tipo_usuario (TIPO) VALUES ('Administrador'), ('Secretario');

INSERT INTO usuario (USUARIO, CONTRASENA_HASH, CEDULA, TIPO_USUARIO_COD_TIPO_USUARIO) VALUES
('admin', 'pbkdf2$210000$+72GwpbbO0rrrKzhpCKMaA==$o8iCYRLHkvHcfgsxZI9dc+bh9es7uS6ib50yaf4nBFA=', '1722601810', 1);

INSERT INTO periodos (INICIO, FIN, ES_ACTUAL) VALUES ('2025-01-01', '2026-12-31', TRUE);

INSERT INTO cargos (CARGO) VALUES
('Presidente'), ('Vicepresidente'), ('Secretario'), ('Tesorero'), ('Vocal');

INSERT INTO comites (COMITE, PERIODOS_COD_PERIODO) VALUES
('Directiva', 1),
('Comisión Académica', 1),
('Comisión Social', 1),
('Comisión Deportiva', 1),
('Comisión Cultural', 1);

INSERT INTO miembros (NOMBRE, CEDULA, TELEFONO, CELULAR, CORREO, CARGOS_COD_CARGO) VALUES
('Andrea Salazar',  '1729083012', '022345678', '0991234501', 'andrea.salazar@example.com', 1),
('Mateo Cevallos',  '1736131861', '022345679', '0991234502', 'mateo.cevallos@example.com', 2),
('Lucía Andrade',   '1709139099', '022345680', '0991234503', 'lucia.andrade@example.com', 3),
('Diego Paredes',   '1746030822', '022345681', '0991234504', 'diego.paredes@example.com', 4),
('Valeria Moreno',  '1726281940', '022345682', '0991234505', 'valeria.moreno@example.com', 1),
('Sebastián Vega',  '1742199357', '022345683', '0991234506', 'sebastian.vega@example.com', 5),
('Camila Herrera',  '1708190937', '022345684', '0991234507', 'camila.herrera@example.com', 5),
('Tomás Ibarra',    '1738657970', '022345685', '0991234508', 'tomas.ibarra@example.com', 1),
('Paula Navarrete', '1724323199', '022345686', '0991234509', 'paula.navarrete@example.com', 3),
('Joaquín Ruiz',    '1728757491', '022345687', '0991234510', 'joaquin.ruiz@example.com', 5);

INSERT INTO miembros_comite (COMITES_COD_COMITE, MIEMBROS_COD_MIEMBRO) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 5), (2, 6), (2, 7),
(3, 8), (3, 9), (3, 10);

INSERT INTO tipo_reunion (TIPO) VALUES ('Ordinaria'), ('Extraordinaria');
INSERT INTO tipo_resolucion (TIPO) VALUES ('Académica'), ('Administrativa');

INSERT INTO reuniones (MOTIVO, FECHA, HORA_INICIO, HORA_FIN, LUGAR, ESTADO, PERIODOS_COD_PERIODO,
                       TIPO_REUNION_COD_TIPO_REUNION, USUARIO_COD_USUARIO, COD_COMITE) VALUES
('Planificación anual de actividades', '2025-03-10', '09:00:00', '10:30:00', 'Sala de reuniones A', 'cerrada', 1, 1, 1, 1),
('Revisión de la malla curricular', '2025-04-02', '15:00:00', '16:30:00', 'Aula 204', 'cerrada', 1, 1, 1, 2),
('Organización de la feria de integración', '2025-05-20', '10:00:00', '11:00:00', 'Auditorio principal', 'programada', 1, 2, 1, 1);

INSERT INTO temas (NUMERO, TEMA, REUNIONES_COD_REUNION) VALUES
(1, 'Lectura y aprobación del orden del día', 1),
(2, 'Presupuesto del año', 1),
(3, 'Cronograma de eventos', 1),
(1, 'Diagnóstico de la malla vigente', 2),
(2, 'Propuesta de nuevas asignaturas optativas', 2),
(1, 'Fecha y lugar de la feria', 3),
(2, 'Asignación de responsables', 3);

INSERT INTO asistencias (PERIODOS_COD_PERIODO, MIEMBROS_COD_MIEMBRO, COMITES_COD_COMITE, REUNIONES_COD_REUNION) VALUES
(1, 1, 1, 1), (1, 2, 1, 1), (1, 3, 1, 1),
(1, 5, 2, 2), (1, 6, 2, 2), (1, 7, 2, 2);

INSERT INTO intervenciones (INTERVENCION, REUNIONES_COD_REUNION, MIEMBROS_COD_MIEMBRO, TEMAS_COD_TEMA) VALUES
('Se da lectura al orden del día y se aprueba por unanimidad.', 1, 1, 1),
('Propone destinar el 40 % del presupuesto a actividades académicas.', 1, 4, 2),
('Sugiere publicar el cronograma en la cartelera y en redes sociales.', 1, 3, 3),
('Presenta los resultados de la encuesta a estudiantes sobre la malla.', 2, 5, 4),
('Propone incluir Programación Web y Ciencia de Datos como optativas.', 2, 6, 5);

INSERT INTO resoluciones (RESOLUCION, TIPO_RESOLUCION_COD_TIPO_RESOLUCION, REUNIONES_COD_REUNION, TEMAS_COD_TEMA) VALUES
('Aprobar el presupuesto anual con la distribución propuesta.', 2, 1, 2),
('Publicar el cronograma de eventos antes del 31 de marzo.', 2, 1, 3),
('Enviar la propuesta de optativas al Consejo Académico.', 1, 2, 5);
