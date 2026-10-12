USE biblioteca;

CREATE TABLE IF NOT EXISTS historial_prestamos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_prestamo INT,
    id_estudiante INT,
    id_libro INT,
    accion VARCHAR(30) NOT NULL,          -- PRESTAMO, DEVOLUCION, DEVOLUCION_ATRASADA, CONSTANCIA_ATRASO
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dias_atraso INT DEFAULT 0,
    detalle VARCHAR(255),
    FOREIGN KEY (id_prestamo) REFERENCES prestamos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id) ON DELETE CASCADE,
    FOREIGN KEY (id_libro) REFERENCES libros(id) ON DELETE CASCADE
);
