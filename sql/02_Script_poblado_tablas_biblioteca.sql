USE biblioteca;

INSERT INTO usuarios (nombre, rut, correo, contraseña, rol) VALUES
('Antonia Pérez', '12345678-9', 'antonia@correo.cl', 'clave123', 'bibliotecario'),
('Carlos Ruiz', '98765432-1', 'carlos@correo.cl', 'clave123', 'estudiante'),
('María Torres', '11222333-4', 'maria@correo.cl', 'clave123', 'estudiante'),
('Ignacio Silva', '22334455-6', 'ignacio@correo.cl', 'clave123', 'estudiante'),
('Laura Méndez', '33445566-7', 'laura@correo.cl', 'clave123', 'estudiante'),
('Javier Soto', '44556677-8', 'javier@correo.cl', 'clave123', 'estudiante'),
('Fernanda Ríos', '55667788-9', 'fer@correo.cl', 'clave123', 'estudiante'),
('Pedro Lagos', '66778899-0', 'pedro@correo.cl', 'clave123', 'estudiante'),
('Valentina Jara', '77889900-1', 'valen@correo.cl', 'clave123', 'estudiante'),
('Tomás Vidal', '88990011-2', 'tomas@correo.cl', 'clave123', 'estudiante');

INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES
('Carlos Ruiz', '98765432-1', '3ro Medio A', 'carlos@correo.cl'),
('María Torres', '11222333-4', '4to Medio B', 'maria@correo.cl'),
('Luis Gómez', '19283746-5', '2do Medio C', 'luis@correo.cl'),
('Ignacio Silva', '22334455-6', '1ro Medio A', 'ignacio@correo.cl'),
('Laura Méndez', '33445566-7', '3ro Medio B', 'laura@correo.cl'),
('Javier Soto', '44556677-8', '4to Medio A', 'javier@correo.cl'),
('Fernanda Ríos', '55667788-9', '2do Medio B', 'fer@correo.cl'),
('Pedro Lagos', '66778899-0', '1ro Medio C', 'pedro@correo.cl'),
('Valentina Jara', '77889900-1', '3ro Medio C', 'valen@correo.cl'),
('Tomás Vidal', '88990011-2', '4to Medio C', 'tomas@correo.cl');

INSERT INTO categorias (nombre) VALUES
('Ciencia Ficción'),
('Historia'),
('Filosofía'),
('Literatura'),
('Tecnología');

INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES
('Dune', 'Frank Herbert', '9780441013593', 'Ace Books', 5, 1),
('Fundación', 'Isaac Asimov', '9788497594256', 'Debolsillo', 3, 1),
('Breve Historia del Tiempo', 'Stephen Hawking', '9780553176988', 'Debate', 2, 2),
('El mundo de Sofía', 'Jostein Gaarder', '9788478884452', 'Siruela', 4, 3),
('1984', 'George Orwell', '9780451524935', 'Penguin Books', 6, 4),
('Crónica de una muerte anunciada', 'Gabriel García Márquez', '9780307387340', 'Sudamericana', 3, 4),
('Introducción a la Inteligencia Artificial', 'Stuart Russell', '9780136042594', 'Pearson', 2, 5),
('Fahrenheit 451', 'Ray Bradbury', '9781451673319', 'Simon & Schuster', 5, 1),
('Sapiens', 'Yuval Noah Harari', '9788499924211', 'Debate', 4, 2),
('Más allá del bien y del mal', 'Friedrich Nietzsche', '9788420688114', 'Alianza', 3, 3);

INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) VALUES
(1, 1, '2025-06-15', '2025-06-22', FALSE),
(2, 2, '2025-06-10', '2025-06-17', TRUE),
(3, 3, '2025-06-20', '2025-06-27', FALSE),
(4, 4, '2025-06-22', '2025-06-29', FALSE),
(5, 5, '2025-06-18', '2025-06-25', TRUE),
(6, 6, '2025-06-12', '2025-06-19', FALSE),
(7, 7, '2025-06-05', '2025-06-12', TRUE),
(8, 8, '2025-06-21', '2025-06-28', FALSE),
(9, 9, '2025-06-16', '2025-06-23', TRUE),
(10, 10, '2025-06-19', '2025-06-26', FALSE);
