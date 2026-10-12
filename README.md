# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio en **Java (Swing / JFrame)** con base de datos **MySQL (JDBC)** para gestionar el préstamo y la devolución de libros de una biblioteca escolar.

Proyecto de la **Evaluación Final Transversal** de *Desarrollo Orientado a Objetos II*.

## Funcionalidades

| Módulo | Bibliotecario | Estudiante |
|---|---|---|
| Autenticación (correo o RUT + contraseña) | ✅ | ✅ |
| Gestión de libros (CRUD + búsqueda) | ✅ | — |
| Gestión de categorías (CRUD) | ✅ | — |
| Gestión de estudiantes (CRUD + cuenta de acceso) | ✅ | — |
| Préstamos (hilo en segundo plano, vencimiento automático) | ✅ a cualquier estudiante | ✅ para sí mismo (Catálogo) |
| Devoluciones con detección de atraso | ✅ | ✅ las propias |
| Constancia de atraso en el historial | ✅ | — |
| Reportes: más prestados, historial por estudiante, libros en préstamo (+ exportar CSV) | ✅ | Historial propio |
| Prueba de préstamos simultáneos (concurrencia) | ✅ | — |

## Tecnologías y patrones

- **Java 17+**, Swing (JFrame, CardLayout, JTable), look & feel Nimbus.
- **MySQL 8** + **MySQL Connector/J** (JDBC, `PreparedStatement`).
- **MVC**: `model` (entidades y lógica), `view` (pantallas Swing), `controller` (intermediarios).
- **DAO**: un DAO por tabla que implementa la interfaz genérica `CrudDAO<T>`.
- **Singleton**: `DatabaseConnection.getInstance()` (única conexión), también `PrestamoService` y `Sesion`.
- **POO**: clases abstractas `Persona`, `Usuario` y `BaseDAO`; herencia `Bibliotecario`/`UsuarioEstudiante`/`Estudiante`; interfaces `Validable`, `CrudDAO<T>`, `Reporte`, `Refrescable`; polimorfismo en `validar()`, `puedeGestionarInventario()` y `Reporte.generar()`.
- **Concurrencia**: `RegistroPrestamoTask implements Runnable` ejecutado con `new Thread(...)`; métodos `synchronized` en `PrestamoService` + transacción con `SELECT ... FOR UPDATE` y `UPDATE ... WHERE stock > 0`.

## Estructura

```
BibliotecaEscolar/
├── pom.xml                       ← dependencias (MySQL Connector/J) y generación del .jar
├── ejecutar.bat                  ← ejecuta el .jar generado
├── sql/
│   ├── 01_Script_crea_tablas_biblioteca.sql
│   ├── 02_Script_poblado_tablas_biblioteca.sql
│   └── 03_Script_tabla_historial.sql   (opcional: la app la crea sola)
└── src/main/
    ├── resources/db.properties   ← usuario y contraseña de MySQL
    └── java/cl/duoc/biblioteca/
        ├── Main.java
        ├── config/      DatabaseConnection (Singleton)
        ├── model/       Persona, Usuario, Bibliotecario, UsuarioEstudiante, Estudiante, Libro, Categoria, Prestamo...
        ├── dao/         CrudDAO, BaseDAO, UsuarioDAO, EstudianteDAO, LibroDAO, CategoriaDAO, PrestamoDAO, HistorialDAO, ReporteDAO
        ├── service/     PrestamoService (synchronized), RegistroPrestamoTask (hilo), PruebaConcurrencia, Sesion
        ├── controller/  LoginController, LibroController, CategoriaController, EstudianteController, PrestamoController, ReporteController
        ├── reporte/     Reporte (interfaz) y sus 3 implementaciones
        ├── view/        LoginFrame, MainFrame y paneles de cada módulo
        ├── exception/   BibliotecaException, ValidacionException, PrestamoException
        └── util/        ErroresBD
```

## Cómo ejecutarlo

1. **Base de datos**: en MySQL Workbench ejecuta, en orden, `sql/01_...` y `sql/02_...` (el `03_...` es opcional).
2. **Conexión**: edita `src/main/resources/db.properties` con tu usuario y contraseña de MySQL.
3. **IntelliJ IDEA**: *File → Open* y elige la carpeta del proyecto (detecta el `pom.xml` y descarga el driver de MySQL).
4. Ejecuta `cl.duoc.biblioteca.Main`.
5. **Ejecutable**: en la pestaña *Maven* → *Lifecycle* → doble clic en **package**. Se genera `target/BibliotecaEscolar-jar-with-dependencies.jar` (se abre con doble clic o con `ejecutar.bat`).
   Si pones un `db.properties` al lado del `.jar`, ese archivo tiene prioridad.

### Usuarios de prueba (contraseña `clave123`)

| Rol | Correo | RUT |
|---|---|---|
| Bibliotecario | antonia@correo.cl | 12345678-9 |
| Estudiante | carlos@correo.cl | 98765432-1 |

## Concurrencia y consistencia del stock

- **Hilo independiente**: al presionar *Registrar préstamo*, `PrestamoController` crea `new Thread(new RegistroPrestamoTask(...))`. El hilo registra el préstamo, descuenta el stock y escribe el historial; la interfaz sigue respondiendo (el reloj de la barra inferior no se detiene). El resultado vuelve a la interfaz con `SwingUtilities.invokeLater`.
- **Sincronización**: `PrestamoService.registrarPrestamo(...)` y `registrarDevolucion(...)` son `synchronized` sobre una instancia única (Singleton), así que dos préstamos nunca modifican el stock al mismo tiempo.
- **Transacción**: verificar stock + insertar préstamo + descontar stock + historial ocurren en una sola transacción (`commit`/`rollback`).
- **Prueba**: *Préstamos → Prueba de concurrencia...* lanza N hilos que piden el mismo libro a la vez (con un `CountDownLatch`). Con stock 5 y 8 hilos, el resultado esperado es 5 aceptados, 3 rechazados y stock final 0, nunca negativo. Al terminar, los préstamos de prueba se revierten.

## Notas de diseño

- En la tabla `prestamos`, la columna `fecha_devolucion` se usa como **fecha de vencimiento** (igual que en los datos de ejemplo: préstamo + 7 días).
- La devolución real y los atrasos quedan registrados en la tabla `historial_prestamos` (acciones `PRESTAMO`, `DEVOLUCION`, `DEVOLUCION_ATRASADA`, `CONSTANCIA_ATRASO`).
- Un estudiante puede tener como máximo 3 libros sin devolver y no puede pedir dos veces el mismo libro.
- No se eliminan libros ni estudiantes que tengan préstamos en su historial, para conservar la trazabilidad.
