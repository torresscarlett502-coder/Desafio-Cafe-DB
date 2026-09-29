-- =====================================================================
-- Cafe Don Bosco -------

CREATE DATABASE IF NOT EXISTS cafe_don_bosco
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cafe_don_bosco;

-- ---------------------------------------------------------------------
-- Usuario: administradores y consumidores registrados
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(80)  NOT NULL,
    apellido    VARCHAR(80)  NOT NULL,
    correo      VARCHAR(120) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    rol         ENUM('ADMINISTRADOR', 'CONSUMIDOR') NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Categoria: agrupacion de productos del catalogo
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(60)  NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Producto: cafe, bebidas, postres y comida del catalogo.
-- tiempo_preparacion_minutos es numerico para poder calcular la ETA de
-- un pedido; el texto que ve el consumidor ("3 minutos") se construye
-- en el DTO a partir de este valor, no se guarda como texto libre.
-- UNIQUE(categoria_id, nombre): evita productos duplicados dentro de
-- la misma categoria sin impedir el mismo nombre en categorias
-- distintas (ej. "Combo" en Cafe y en Comida).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    categoria_id                INT NOT NULL,
    nombre                      VARCHAR(120) NOT NULL,
    descripcion                 VARCHAR(500),
    precio                      DECIMAL(10,2) NOT NULL,
    imagen                      VARCHAR(255),
    tiempo_preparacion_minutos  INT,
    activo                      BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en                   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT uk_producto_categoria_nombre UNIQUE (categoria_id, nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Inventario: existencias por producto (1 a 1 con producto)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventario (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    producto_id     INT NOT NULL UNIQUE,
    cantidad        INT NOT NULL DEFAULT 0,
    stock_minimo    INT NOT NULL DEFAULT 5,
    CONSTRAINT fk_inventario_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT chk_inventario_cantidad CHECK (cantidad >= 0),
    CONSTRAINT chk_inventario_stock_minimo CHECK (stock_minimo >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Venta: modelo unificado para ventas presenciales (POS) y pedidos web.
-- TipoVenta distingue el origen; el consumidor invitado no requiere
-- usuario_id, por lo que se guardan los datos de contacto en la venta.
--
-- `estado` es el ciclo del PEDIDO (RECIBIDO..ENTREGADO/CANCELADO) y
-- `estado_pago` el ciclo del PAGO (PENDIENTE/APROBADO/RECHAZADO): son
-- conceptos independientes, nunca se mezclan.
--
-- idempotency_key evita procesar dos veces un mismo checkout enviado
-- por error dos veces (doble clic, reintento de red).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venta (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id                  INT NULL,
    tipo_venta                  ENUM('PRESENCIAL', 'WEB') NOT NULL,
    estado                      ENUM('RECIBIDO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO')
                                    NOT NULL DEFAULT 'RECIBIDO',
    subtotal                    DECIMAL(10,2) NOT NULL,
    envio                       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total                       DECIMAL(10,2) NOT NULL,
    metodo_pago                 VARCHAR(30),
    estado_pago                 ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    tipo_entrega                VARCHAR(20),
    nombre_cliente               VARCHAR(150),
    correo_cliente               VARCHAR(120),
    telefono_cliente             VARCHAR(30),
    direccion_cliente            VARCHAR(255),
    notas                       VARCHAR(500),
    token_ticket                VARCHAR(64) NOT NULL UNIQUE,
    idempotency_key              VARCHAR(80) NULL UNIQUE,
    fecha                       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio_preparacion     TIMESTAMP NULL,
    fecha_estimada_listo         TIMESTAMP NULL,
    fecha_listo                 TIMESTAMP NULL,
    fecha_entregado              TIMESTAMP NULL,
    actualizado_en               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_venta_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE INDEX idx_venta_usuario_id ON venta(usuario_id);
CREATE INDEX idx_venta_estado ON venta(estado);
CREATE INDEX idx_venta_estado_pago ON venta(estado_pago);
CREATE INDEX idx_venta_tipo_venta ON venta(tipo_venta);
CREATE INDEX idx_venta_fecha ON venta(fecha);

-- ---------------------------------------------------------------------
-- DetalleVenta: productos incluidos en cada venta (precio historico)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_venta (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    venta_id            INT NOT NULL,
    producto_id         INT NOT NULL,
    nombre_producto     VARCHAR(120) NOT NULL,
    cantidad            INT NOT NULL,
    precio_unitario     DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalleventa_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalleventa_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT chk_detalleventa_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_detalleventa_venta_id ON detalle_venta(venta_id);
CREATE INDEX idx_detalleventa_producto_id ON detalle_venta(producto_id);

-- ---------------------------------------------------------------------
-- Proveedor: entidad propia en vez de texto libre en compra.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS proveedor (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL UNIQUE,
    contacto    VARCHAR(120),
    telefono    VARCHAR(30),
    correo      VARCHAR(120),
    direccion   VARCHAR(255),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Compra: ingreso de mercaderia registrado por el administrador.
-- proveedor_nombre es una fotografia del nombre al momento de la
-- compra (igual criterio que detalle_venta.nombre_producto).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS compra (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    proveedor_id        INT NOT NULL,
    proveedor_nombre    VARCHAR(150) NOT NULL,
    usuario_id          INT NOT NULL,
    total               DECIMAL(10,2) NOT NULL,
    fecha               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_compra_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
    CONSTRAINT fk_compra_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- DetalleCompra: productos ingresados en cada compra
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_compra (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    compra_id           INT NOT NULL,
    producto_id         INT NOT NULL,
    cantidad            INT NOT NULL,
    costo_unitario      DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detallecompra_compra
        FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    CONSTRAINT fk_detallecompra_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT chk_detallecompra_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detallecompra_costo CHECK (costo_unitario > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- MovimientoInventario: registro inmutable de cada cambio de stock.
-- venta_id/compra_id son excluyentes; ambos pueden ser NULL cuando el
-- movimiento es un ajuste manual del administrador.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    producto_id         INT NOT NULL,
    tipo_movimiento     ENUM('ENTRADA', 'SALIDA', 'AJUSTE', 'DEVOLUCION') NOT NULL,
    cantidad            INT NOT NULL,
    stock_anterior      INT NOT NULL,
    stock_nuevo         INT NOT NULL,
    motivo              VARCHAR(255),
    venta_id            INT NULL,
    compra_id           INT NULL,
    usuario_id          INT NULL,
    fecha               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimiento_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_movimiento_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),
    CONSTRAINT fk_movimiento_compra
        FOREIGN KEY (compra_id) REFERENCES compra(id),
    CONSTRAINT fk_movimiento_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_movimiento_producto_id ON movimiento_inventario(producto_id);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario(fecha);

-- ---------------------------------------------------------------------
-- Bitacora: auditoria de operaciones administrativas importantes.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bitacora (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT NULL,
    accion      VARCHAR(60) NOT NULL,
    entidad     VARCHAR(60) NOT NULL,
    entidad_id  INT,
    detalle     VARCHAR(500),
    fecha       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bitacora_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Horario de atencion: un pedido (web o presencial) solo se acepta si
-- la hora actual en El Salvador cae dentro del horario del dia y ese
-- canal esta habilitado. permitir_pedidos_app/local tambien sirve para
-- cerrar un canal por completo un dia especifico (feriado, mantenimiento)
-- sin tener que borrar el horario configurado.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS horario_atencion (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    dia_semana              TINYINT NOT NULL,   -- 1 = Lunes ... 7 = Domingo (DayOfWeek.getValue())
    nombre_dia              VARCHAR(20) NOT NULL,
    hora_apertura           TIME NOT NULL,
    hora_cierre             TIME NOT NULL,
    permitir_pedidos_app    BOOLEAN NOT NULL DEFAULT TRUE,
    permitir_pedidos_local  BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_horario_dia UNIQUE (dia_semana),
    CONSTRAINT chk_horario_dia_semana CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT chk_horario_rango CHECK (hora_cierre > hora_apertura)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Personalizacion de productos: un producto puede ofrecer uno o mas
-- grupos de opciones (ej. "Tipo de leche", "Nivel de azucar"). Un grupo
-- de seleccion_multiple=FALSE solo admite elegir una opcion (tipo de
-- leche); uno con seleccion_multiple=TRUE admite varias (jarabes
-- extra). obligatorio=TRUE exige elegir al menos una opcion de ese
-- grupo antes de poder agregar el producto al carrito.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS grupo_opcion (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(50) NOT NULL UNIQUE,
    obligatorio         BOOLEAN NOT NULL DEFAULT FALSE,
    seleccion_multiple  BOOLEAN NOT NULL DEFAULT FALSE,
    activo              BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS opcion (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    grupo_id          INT NOT NULL,
    nombre            VARCHAR(50) NOT NULL,
    precio_adicional  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_opcion_grupo
        FOREIGN KEY (grupo_id) REFERENCES grupo_opcion(id) ON DELETE CASCADE,
    CONSTRAINT chk_opcion_precio CHECK (precio_adicional >= 0),
    CONSTRAINT uk_opcion_grupo_nombre UNIQUE (grupo_id, nombre)
) ENGINE=InnoDB;

-- Que grupos de opciones ofrece cada producto (un grupo se puede
-- reutilizar en varios productos, ej. "Tipo de leche" en todos los
-- cafes; un producto puede tener varios grupos, ej. cafe + tipo de
-- leche + nivel de azucar).
CREATE TABLE IF NOT EXISTS producto_grupo_opcion (
    producto_id  INT NOT NULL,
    grupo_id     INT NOT NULL,
    PRIMARY KEY (producto_id, grupo_id),
    CONSTRAINT fk_productogrupo_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT fk_productogrupo_grupo
        FOREIGN KEY (grupo_id) REFERENCES grupo_opcion(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Fotografia de las opciones elegidas en una linea de venta ya
-- facturada (mismo criterio que detalle_venta.nombre_producto): si la
-- opcion cambia de nombre o precio despues, el historial de ventas no
-- debe cambiar retroactivamente. opcion_id puede quedar NULL si la
-- opcion original se borra mas adelante; el nombre y el precio ya
-- quedaron copiados aqui.
CREATE TABLE IF NOT EXISTS detalle_venta_opcion (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    detalle_venta_id  INT NOT NULL,
    opcion_id         INT NULL,
    nombre_grupo      VARCHAR(50) NOT NULL,
    nombre_opcion     VARCHAR(50) NOT NULL,
    precio_aplicado   DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalleventaopcion_detalle
        FOREIGN KEY (detalle_venta_id) REFERENCES detalle_venta(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalleventaopcion_opcion
        FOREIGN KEY (opcion_id) REFERENCES opcion(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_detalleventaopcion_detalle_id ON detalle_venta_opcion(detalle_venta_id);

-- ---------------------------------------------------------------------
-- Datos iniciales
-- ---------------------------------------------------------------------

-- Administrador por defecto (password: Admin123! -- cambiar en produccion)
-- Hash BCrypt generado con PasswordUtil.hashear("Admin123!")
INSERT INTO usuario (nombre, apellido, correo, password, rol, activo)
VALUES ('Scarlett', 'Torres', 'admin@cafedonbosco.com',
        '$2a$12$B4q7wzKyFepDwVIgCCjfCesC3unEGtvFQIN3HwrlliAe4e88f6lGq',
        'ADMINISTRADOR', TRUE)
ON DUPLICATE KEY UPDATE correo = correo;

INSERT INTO categoria (nombre, descripcion, activo) VALUES
    ('Cafe', 'Bebidas a base de espresso', TRUE),
    ('Bebidas', 'Bebidas frias y calientes', TRUE),
    ('Postres', 'Reposteria y dulces', TRUE),
    ('Comida', 'Sandwiches y snacks', TRUE)
ON DUPLICATE KEY UPDATE nombre = nombre;

-- Productos de ejemplo para poder probar el catalogo sin cargar datos a mano
INSERT INTO producto (categoria_id, nombre, descripcion, precio, imagen, tiempo_preparacion_minutos, activo)
SELECT id, 'Cafe Latte', 'Cafe espresso con leche vaporizada.', 2.50, '/assets/img/capuchino.jpg', 3, TRUE
FROM categoria WHERE nombre = 'Cafe'
UNION ALL
SELECT id, 'Cafe Americano', 'Cafe puro, de sabor intenso.', 2.00, '/assets/img/americano.jpg', 2, TRUE
FROM categoria WHERE nombre = 'Cafe'
UNION ALL
SELECT id, 'Frappe de vainilla', 'Refrescante y cremoso.', 3.00, '/assets/img/frappe-vainilla.jpg', 4, TRUE
FROM categoria WHERE nombre = 'Bebidas'
UNION ALL
SELECT id, 'Pastel de chocolate', 'Suave, intenso y delicioso.', 3.50, NULL, 1, TRUE
FROM categoria WHERE nombre = 'Postres'
UNION ALL
SELECT id, 'Croissant', 'Hojaldre artesanal.', 2.00, '/assets/img/croissant.jpg', 1, TRUE
FROM categoria WHERE nombre = 'Comida'
ON DUPLICATE KEY UPDATE imagen = VALUES(imagen);

INSERT INTO inventario (producto_id, cantidad, stock_minimo)
SELECT id, 30, 5 FROM producto WHERE NOT EXISTS (
    SELECT 1 FROM inventario WHERE inventario.producto_id = producto.id
);

INSERT INTO proveedor (nombre, contacto, telefono, correo, direccion, activo)
VALUES ('Distribuidora Cafetalera S.A.', 'Juan Perez', '+503 2222 3333',
        'ventas@distribuidoracafetalera.com', 'San Salvador', TRUE)
ON DUPLICATE KEY UPDATE nombre = nombre;

INSERT INTO horario_atencion (dia_semana, nombre_dia, hora_apertura, hora_cierre) VALUES
    (1, 'LUNES', '06:30:00', '20:30:00'),
    (2, 'MARTES', '06:30:00', '20:30:00'),
    (3, 'MIERCOLES', '06:30:00', '20:30:00'),
    (4, 'JUEVES', '06:30:00', '20:30:00'),
    (5, 'VIERNES', '06:30:00', '21:30:00'),
    (6, 'SABADO', '07:00:00', '21:30:00'),
    (7, 'DOMINGO', '07:00:00', '19:00:00')
ON DUPLICATE KEY UPDATE dia_semana = dia_semana;

-- Personalizacion de ejemplo: tipo de leche (seleccion unica, opcional)
-- y nivel de azucar (seleccion unica, opcional), aplicados a los cafes.
INSERT INTO grupo_opcion (nombre, obligatorio, seleccion_multiple, activo) VALUES
    ('Tipo de leche', FALSE, FALSE, TRUE),
    ('Nivel de azucar', FALSE, FALSE, TRUE)
ON DUPLICATE KEY UPDATE nombre = nombre;

INSERT INTO opcion (grupo_id, nombre, precio_adicional, activo)
SELECT id, 'Leche entera', 0.00, TRUE FROM grupo_opcion WHERE nombre = 'Tipo de leche'
UNION ALL
SELECT id, 'Leche deslactosada', 0.00, TRUE FROM grupo_opcion WHERE nombre = 'Tipo de leche'
UNION ALL
SELECT id, 'Leche de almendra', 0.50, TRUE FROM grupo_opcion WHERE nombre = 'Tipo de leche'
UNION ALL
SELECT id, 'Leche de avena', 0.50, TRUE FROM grupo_opcion WHERE nombre = 'Tipo de leche'
UNION ALL
SELECT id, 'Normal', 0.00, TRUE FROM grupo_opcion WHERE nombre = 'Nivel de azucar'
UNION ALL
SELECT id, 'Sin azucar', 0.00, TRUE FROM grupo_opcion WHERE nombre = 'Nivel de azucar'
UNION ALL
SELECT id, 'Extra dulce', 0.00, TRUE FROM grupo_opcion WHERE nombre = 'Nivel de azucar'
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO producto_grupo_opcion (producto_id, grupo_id)
SELECT p.id, g.id
FROM producto p
JOIN categoria c ON c.id = p.categoria_id AND c.nombre = 'Cafe'
JOIN grupo_opcion g ON g.nombre IN ('Tipo de leche', 'Nivel de azucar')
ON DUPLICATE KEY UPDATE producto_id = VALUES(producto_id);

-- Personalizacion adicional: tamano y extras para bebidas (Cafe + Bebidas,
-- seleccion unica obligatoria para el tamano, multiple opcional para
-- extras), y acompanamientos para Comida (multiple, opcional). El Pastel
-- de chocolate (Postres) queda deliberadamente sin ningun grupo: no todos
-- los productos necesitan personalizacion.
INSERT INTO grupo_opcion (nombre, obligatorio, seleccion_multiple, activo) VALUES
    ('Tamano', TRUE, FALSE, TRUE),
    ('Extras', FALSE, TRUE, TRUE),
    ('Acompanamientos', FALSE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO opcion (grupo_id, nombre, precio_adicional, activo)
SELECT id, 'Pequeno', 0.00, TRUE FROM grupo_opcion WHERE nombre = 'Tamano'
UNION ALL
SELECT id, 'Mediano', 0.50, TRUE FROM grupo_opcion WHERE nombre = 'Tamano'
UNION ALL
SELECT id, 'Grande', 1.00, TRUE FROM grupo_opcion WHERE nombre = 'Tamano'
UNION ALL
SELECT id, 'Shot adicional', 0.50, TRUE FROM grupo_opcion WHERE nombre = 'Extras'
UNION ALL
SELECT id, 'Canela', 0.25, TRUE FROM grupo_opcion WHERE nombre = 'Extras'
UNION ALL
SELECT id, 'Crema batida', 0.50, TRUE FROM grupo_opcion WHERE nombre = 'Extras'
UNION ALL
SELECT id, 'Mermelada', 0.25, TRUE FROM grupo_opcion WHERE nombre = 'Acompanamientos'
UNION ALL
SELECT id, 'Mantequilla extra', 0.15, TRUE FROM grupo_opcion WHERE nombre = 'Acompanamientos'
UNION ALL
SELECT id, 'Miel', 0.25, TRUE FROM grupo_opcion WHERE nombre = 'Acompanamientos'
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO producto_grupo_opcion (producto_id, grupo_id)
SELECT p.id, g.id
FROM producto p
JOIN categoria c ON c.id = p.categoria_id AND c.nombre IN ('Cafe', 'Bebidas')
JOIN grupo_opcion g ON g.nombre IN ('Tamano', 'Extras')
ON DUPLICATE KEY UPDATE producto_id = VALUES(producto_id);

INSERT INTO producto_grupo_opcion (producto_id, grupo_id)
SELECT p.id, g.id
FROM producto p
JOIN categoria c ON c.id = p.categoria_id AND c.nombre = 'Comida'
JOIN grupo_opcion g ON g.nombre = 'Acompanamientos'
ON DUPLICATE KEY UPDATE producto_id = VALUES(producto_id);
