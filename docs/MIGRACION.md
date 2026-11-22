# Análisis y migración a Java 21

Este documento resume el estado del sistema original (2009), la viabilidad de llevarlo a Java 21 y
las decisiones tomadas en la modernización.

## 1. Punto de partida

| Elemento | Estado encontrado |
|---|---|
| Plataforma | Java 1.5 (`javac.source=1.5`), proyecto NetBeans 6 con Ant (`nbproject/`) |
| Dependencias | JARs sueltos referenciados con rutas absolutas de Windows (`C:\Documents and Settings\...`) |
| Librerías | MySQL Connector/J 5.0.7 **y** 5.0.8 a la vez, JasperReports 2.0.3, commons-* de 2004–2006, beansbinding, AbsoluteLayout |
| Base de datos | Volcado phpMyAdmin de MySQL 5.0: 15 tablas MyISAM en latin1, sin claves foráneas |
| Código | ~3.200 líneas en 17 clases; la lógica vivía dentro de los manejadores de eventos de los formularios |
| Artefactos | `build/`, `dist/` y 4 volcados `hs_err_pid*.log` de la JVM versionados junto al código |
| Código fuente faltante | `reportes.IfrReporActas` y `reportes.CargaReporte` solo existían como `.class` compilados |
| Pruebas | Ninguna |

### Viabilidad de Java 21

La migración era **viable sin cambiar de tecnología de interfaz**: Swing sigue incluido en Java 21
y el código generado por el diseñador de NetBeans (`GroupLayout`) compila sin cambios. Los
bloqueos reales estaban en otro lado:

1. **Build no reproducible**: rutas absolutas a JARs de otra máquina → se pasó a Maven.
2. **Driver JDBC obsoleto**: `com.mysql.jdbc.Driver` 5.0 no soporta MySQL 8 (autenticación
   `caching_sha2_password`) → `com.mysql:mysql-connector-j` actual, cargado por SPI (sin `Class.forName`).
3. **Sensibilidad a mayúsculas**: el código usaba `MIEMBROS` y el volcado `miembros`. Funcionaba en
   Windows, pero en Linux/Docker MySQL distingue mayúsculas en nombres de tabla → todas las
   consultas se reescribieron con los nombres reales.
4. **JasperReports 2.0.3**: incompatible con JVMs modernas y con vulnerabilidades conocidas. El único
   reporte con fuente (`report1.jrxml`) apuntaba a una base de otro proyecto (`addressbook`) y a una
   ruta `D:/NetBeansProjects/...`; se eliminó y la funcionalidad de reportes se reimplementó.

## 2. Problemas encontrados y cómo se resolvieron

### 2.1 Inyección SQL en todos los formularios

Todas las consultas se construían concatenando el texto de los campos:

```java
// Original (ifrmMiembros.java)
ad.insertarRegistro("INSERT INTO `actas`.`miembros` (...) VALUES (NULL, '"
        + this.txtNombre.getText() + "', '" + this.txtCedula.getText() + "', ...)");
```

Un nombre con apóstrofo (`O'Brien`) rompía el guardado, y un texto malicioso podía modificar o
borrar datos. Ahora todas las consultas usan parámetros:

```java
// Modernizado (MiembroDao.java)
tx.insert("""
        INSERT INTO miembros (NOMBRE, CEDULA, TELEFONO, CELULAR, CORREO, CARGOS_COD_CARGO)
        VALUES (?, ?, ?, ?, ?, ?)
        """, m.nombre(), m.cedula(), m.telefono(), m.celular(), m.correo(), m.cargoId());
```

`MiembroDaoTest.elNombreSeTrataComoDatoYNoComoSql` lo verifica.

### 2.2 Fugas de conexiones y errores silenciados

```java
// Original (AccesoDatos.java)
public ResultSet getResult(String sentencia) {
    Connection cn = this.getConexion();      // nueva conexión en cada consulta
    ...
    try { rs = st.executeQuery(sentencia); } // nunca se cierra
    catch (Exception e) { mensaje = "Error en la comunicación de datos"; } // nadie lo lee
    finally { return rs; }                    // return en finally: oculta cualquier excepción
}
```

Además, `ModeloTabla`, `ModeloCombo` y casi todos los formularios tenían `catch (Exception e) {}`
vacíos. Si la base no respondía, la aplicación mostraba tablas vacías sin ningún aviso.

La nueva clase `actas.db.Jdbc` cierra conexiones, sentencias y resultados con try-with-resources,
convierte `SQLException` en excepciones no comprobadas (`DataAccessException`,
`IntegrityViolationException`) y `Ui.run(...)` muestra cada error al usuario y lo registra en el log.

### 2.3 Operaciones sin transacción

Cerrar una reunión ejecutaba N `INSERT` de asistencias y un `UPDATE` de estado de forma
independiente; un fallo a mitad dejaba datos inconsistentes. Lo mismo al crear un miembro y su
pertenencia al comité. Ahora `Jdbc.inTransaction(...)` agrupa cada operación compuesta
(`ReunionDaoTest.siFallaUnaAsistenciaSeRevierteTodoElCierre`).

### 2.4 Selección de filas frágil y estado global

Para saber qué registro había pulsado el usuario, los formularios **volvían a consultar toda la
tabla** y tomaban el valor por posición de columna (`a.get(11)` era el comité). La reunión elegida
para registrar el acta viajaba en una variable `public static ArrayList filaSeleccionada`, y el
escritorio MDI era `public static JDesktopPane`.

Ahora `ListTableModel<T>` guarda los objetos del dominio (`Reunion`, `Miembro`...) y la reunión se
pasa por constructor. Las dependencias se crean una vez en `AppContext`.

### 2.5 Valores fijos y errores de lógica

| Original | Corrección |
|---|---|
| Periodo `'2'` y usuario `'4'` fijos en cada `INSERT` de reuniones | Periodo actual desde la tabla `periodos`; usuario de la sesión |
| `UPDATE ... ESTADO = 'programadae'` (errata) al editar una reunión | Enum `EstadoReunion` + restricción `CHECK` en la base |
| Columna `IP` siempre `127.0.0.0` / `127.0.0.1` | Eliminada |
| Miembro identificado por **nombre** al actualizar su comité | Se usa la clave primaria |
| Al marcar/desmarcar un asistente se **agregaba** al combo de presentes (duplicados) | El combo refleja exactamente los marcados |
| Borrar un miembro dejaba registros huérfanos en `miembros_comite` | `ON DELETE CASCADE`; y si tiene actas, la FK lo impide |
| Botón "Aceptar" del login sin acción; clave visible (`JTextField`) | Autenticación real con `JPasswordField` |
| Menús "Usuarios", "Reportes › Actas" y "Reportes › Asistencias" vacíos | Implementados |
| Tipos de resolución comparados con literales `'Academica'` | Se agrupan dinámicamente por tipo |

### 2.6 Contraseñas y credenciales

- La conexión usaba `root` sin contraseña, escrita en el código. Ahora se lee de
  `actas.properties` o de variables de entorno, y `DatabaseConfig.toString()` oculta la clave.
- La tabla `usuario` guardaba `CONTRASENA varchar(15)` en texto plano. Se reemplazó por
  `CONTRASENA_HASH` con PBKDF2-HMAC-SHA256 (210.000 iteraciones, sal aleatoria de 16 bytes),
  implementado solo con la JDK.

### 2.7 Datos personales

El volcado original contenía nombres reales de personas. **No se incluye en el repositorio**; los
datos de `db/02-seed.sql` son completamente ficticios (las cédulas solo cumplen el algoritmo de
verificación para poder probar la validación).

## 3. Cambios en el esquema

| Tabla / columna | Cambio |
|---|---|
| Todas | MyISAM → InnoDB, latin1 → utf8mb4, claves foráneas reales |
| `reuniones.HORA_INICIO`, `HORA_FIN` | `VARCHAR` (`'9h00'`) → `TIME` |
| `reuniones.ESTADO` | `CHECK (ESTADO IN ('programada','cerrada'))` |
| `reuniones.IP` | Eliminada |
| `asistencias/resoluciones.REUNIONES_COD_REUNIONE` | Renombrada a `REUNIONES_COD_REUNION` (errata) |
| `usuario.CONTRASENA` | Reemplazada por `CONTRASENA_HASH VARCHAR(255)` |
| `periodos.ESACTUAL` (`'si'`) | `ES_ACTUAL BOOLEAN` |
| `miembros_periodos` | Eliminada (la aplicación nunca la usó) |
| `temas`, `asistencias`, `miembros_comite` | `UNIQUE` para evitar duplicados |

Se conservaron los nombres de tablas y columnas originales (`COD_MIEMBRO`, `CARGOS_COD_CARGO`...)
para que la correspondencia con el sistema anterior sea directa.

## 4. Correspondencia de clases

| Original | Nuevo |
|---|---|
| `actas.Main` | `actas.App` |
| `actas.FrmPadre` | `actas.ui.MainFrame` |
| `actas.dialogoConfiguracion` / `actas.dlgInicial` (duplicado sin uso) | `actas.ui.LoginDialog` |
| `actas.ifrmMiembros` | `actas.ui.MiembrosFrame` |
| `actas.ifrReuniones` | `actas.ui.ReunionesFrame` |
| `actas.ifrSelReunion` | `actas.ui.SeleccionReunionFrame` |
| `actas.ifrmReunion` | `actas.ui.RegistroActaFrame` |
| `actas.ifrmCerradas` | `actas.ui.ReunionesCerradasFrame` |
| `actas.ifrUsuarios` | `actas.ui.UsuariosFrame` |
| — | `actas.ui.AsistenciasReportFrame` |
| `datos.AccesoDatos` | `actas.db.Jdbc` + `actas.dao.*` |
| `datos.ModeloTabla` | `actas.ui.components.ListTableModel<T>` |
| `datos.ModeloCombo`, `actas.ModeloListaCheck` | `Ui.fill(...)` con `List<Opcion>` / `CheckListItem<T>` |
| `actas.CheckListItem`, `actas.CheckListRenderer` | `actas.ui.components.*` (genéricos) |
| `reportes.NewJFrame`, `report1.jrxml` | Eliminados (prueba de otro proyecto) |

### Sobre el código de interfaz

Los métodos `initComponents()` conservan el layout que generó el diseñador de NetBeans: así la
aplicación mantiene su aspecto original y el cambio se concentra donde estaban los problemas. Se
eliminaron los archivos `.form`, las fuentes fijas `Tahoma` (inexistentes fuera de Windows) y toda
consulta SQL embebida en el layout; los componentes se renombraron (`jButton1` → `btnNuevo`) y se
corrigieron las erratas visibles ("Progrmar Reuiniones", "Conclucion", "COMBOCADOS"...). Reescribir
esos layouts con un gestor adaptable (MigLayout) queda como siguiente paso.

## 5. Verificación

- `./mvnw verify`: 45 pruebas (JUnit 6 + AssertJ + H2 en modo MySQL) y JAR ejecutable.
- Prueba manual contra **MySQL 8.4** en Docker: login, programación de reunión, temas, intervención
  con comillas y ñ, cierre transaccional, rechazo de doble cierre, rechazo de borrado por FK,
  reporte de asistencias y exportación del acta.
- GitHub Actions compila con JDK 21 y valida que los scripts SQL cargan en MySQL 8.4.
