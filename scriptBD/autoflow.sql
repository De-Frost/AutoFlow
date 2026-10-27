/* AutoFlow - Script de la base de datos */

DROP DATABASE IF EXISTS autoflow;
CREATE DATABASE autoflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE autoflow;

/* ---------- Seguridad ---------- */
CREATE TABLE rol (
  id_rol INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(30) NOT NULL,
  PRIMARY KEY (id_rol),
  UNIQUE KEY uk_rol_nombre (nombre)
) ENGINE = InnoDB;

CREATE TABLE usuario (
  id_usuario INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(30) NOT NULL,
  password VARCHAR(512) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  id_rol INT NOT NULL,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uk_usuario_username (username),
  FOREIGN KEY fk_usuario_rol (id_rol) REFERENCES rol (id_rol)
) ENGINE = InnoDB;

/* ---------- Clientes y vehiculos ---------- */
CREATE TABLE cliente (
  id_cliente INT NOT NULL AUTO_INCREMENT,
  identificacion VARCHAR(20) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  telefono VARCHAR(20) NOT NULL,
  correo VARCHAR(75) NULL,
  id_usuario INT NULL,
  PRIMARY KEY (id_cliente),
  UNIQUE KEY uk_cliente_identificacion (identificacion),
  UNIQUE KEY uk_cliente_usuario (id_usuario),
  FOREIGN KEY fk_cliente_usuario (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE vehiculo (
  id_vehiculo INT NOT NULL AUTO_INCREMENT,
  placa VARCHAR(10) NOT NULL,
  marca VARCHAR(50) NOT NULL,
  modelo VARCHAR(50) NOT NULL,
  anio INT NOT NULL,
  id_cliente INT NOT NULL,
  PRIMARY KEY (id_vehiculo),
  UNIQUE KEY uk_vehiculo_placa (placa),
  FOREIGN KEY fk_vehiculo_cliente (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE = InnoDB;

/* ---------- Reservas y recepcion ---------- */
CREATE TABLE espacio (
  id_espacio INT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(10) NOT NULL,
  nombre VARCHAR(50) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_espacio),
  UNIQUE KEY uk_espacio_codigo (codigo)
) ENGINE = InnoDB;

CREATE TABLE reserva (
  id_reserva INT NOT NULL AUTO_INCREMENT,
  fecha DATE NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  motivo VARCHAR(255) NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT "Confirmada",
  motivo_cancelacion VARCHAR(255) NULL,
  id_cliente INT NOT NULL,
  id_vehiculo INT NOT NULL,
  id_espacio INT NOT NULL,
  PRIMARY KEY (id_reserva),
  FOREIGN KEY fk_reserva_cliente (id_cliente) REFERENCES cliente (id_cliente),
  FOREIGN KEY fk_reserva_vehiculo (id_vehiculo) REFERENCES vehiculo (id_vehiculo),
  FOREIGN KEY fk_reserva_espacio (id_espacio) REFERENCES espacio (id_espacio)
) ENGINE = InnoDB;

CREATE TABLE recepcion (
  id_recepcion INT NOT NULL AUTO_INCREMENT,
  fecha_ingreso DATETIME NOT NULL,
  kilometraje INT NOT NULL,
  nivel_combustible VARCHAR(20) NOT NULL,
  danos_visibles VARCHAR(255) NULL,
  objetos_personales VARCHAR(255) NULL,
  falla_reportada VARCHAR(255) NOT NULL,
  id_vehiculo INT NOT NULL,
  id_reserva INT NULL,
  PRIMARY KEY (id_recepcion),
  UNIQUE KEY uk_recepcion_reserva (id_reserva),
  FOREIGN KEY fk_recepcion_vehiculo (id_vehiculo) REFERENCES vehiculo (id_vehiculo),
  FOREIGN KEY fk_recepcion_reserva (id_reserva) REFERENCES reserva (id_reserva)
) ENGINE = InnoDB;

/* ---------- Diagnostico y ordenes ---------- */
CREATE TABLE diagnostico (
  id_diagnostico INT NOT NULL AUTO_INCREMENT,
  fecha DATETIME NOT NULL,
  fallas_encontradas TEXT NOT NULL,
  trabajos_necesarios TEXT NOT NULL,
  id_recepcion INT NOT NULL,
  id_mecanico INT NULL,
  PRIMARY KEY (id_diagnostico),
  UNIQUE KEY uk_diagnostico_recepcion (id_recepcion),
  FOREIGN KEY fk_diagnostico_recepcion (id_recepcion) REFERENCES recepcion (id_recepcion),
  FOREIGN KEY fk_diagnostico_mecanico (id_mecanico) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE orden_servicio (
  id_orden INT NOT NULL AUTO_INCREMENT,
  fecha DATETIME NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT "Pendiente",
  fecha_estado DATETIME NULL,
  total DECIMAL(12,2) NULL,
  id_diagnostico INT NOT NULL,
  id_mecanico INT NULL,
  PRIMARY KEY (id_orden),
  UNIQUE KEY uk_orden_diagnostico (id_diagnostico),
  FOREIGN KEY fk_orden_diagnostico (id_diagnostico) REFERENCES diagnostico (id_diagnostico),
  FOREIGN KEY fk_orden_mecanico (id_mecanico) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE detalle_orden_servicio (
  id_detalle_servicio INT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(150) NOT NULL,
  cantidad INT NOT NULL DEFAULT 1,
  costo DECIMAL(12,2) NOT NULL,
  id_orden INT NOT NULL,
  PRIMARY KEY (id_detalle_servicio),
  FOREIGN KEY fk_detalle_servicio_orden (id_orden) REFERENCES orden_servicio (id_orden)
) ENGINE = InnoDB;

/* ---------- Inventario y facturacion ---------- */
CREATE TABLE repuesto (
  id_repuesto INT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(20) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  precio DECIMAL(12,2) NOT NULL,
  cantidad_disponible INT NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_repuesto),
  UNIQUE KEY uk_repuesto_codigo (codigo),
  CHECK (precio >= 0),
  CHECK (cantidad_disponible >= 0)
) ENGINE = InnoDB;

CREATE TABLE detalle_orden_repuesto (
  id_detalle_repuesto INT NOT NULL AUTO_INCREMENT,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  id_orden INT NOT NULL,
  id_repuesto INT NOT NULL,
  PRIMARY KEY (id_detalle_repuesto),
  FOREIGN KEY fk_detalle_repuesto_orden (id_orden) REFERENCES orden_servicio (id_orden),
  FOREIGN KEY fk_detalle_repuesto_repuesto (id_repuesto) REFERENCES repuesto (id_repuesto)
) ENGINE = InnoDB;

CREATE TABLE movimiento_inventario (
  id_movimiento INT NOT NULL AUTO_INCREMENT,
  tipo VARCHAR(10) NOT NULL,
  cantidad INT NOT NULL,
  stock_anterior INT NULL,
  stock_nuevo INT NULL,
  fecha DATETIME NOT NULL,
  id_repuesto INT NOT NULL,
  id_orden INT NULL,
  id_usuario INT NULL,
  PRIMARY KEY (id_movimiento),
  FOREIGN KEY fk_movimiento_repuesto (id_repuesto) REFERENCES repuesto (id_repuesto),
  FOREIGN KEY fk_movimiento_orden (id_orden) REFERENCES orden_servicio (id_orden),
  FOREIGN KEY fk_movimiento_usuario (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE factura (
  id_factura INT NOT NULL AUTO_INCREMENT,
  fecha DATETIME NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  id_orden INT NOT NULL,
  id_usuario INT NULL,
  PRIMARY KEY (id_factura),
  UNIQUE KEY uk_factura_orden (id_orden),
  FOREIGN KEY fk_factura_orden (id_orden) REFERENCES orden_servicio (id_orden),
  FOREIGN KEY fk_factura_usuario (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

/*--- Datos ----*/
INSERT INTO rol (nombre) VALUES
  ("ADMINISTRADOR"), ("RECEPCIONISTA"), ("MECANICO"), ("CLIENTE");

INSERT INTO espacio (codigo, nombre, tipo, activo) VALUES
  ("E-01", "Elevador 01", "Elevador", TRUE),
  ("E-02", "Bahía 02", "Bahía", TRUE),
  ("E-03", "Bahía 03", "Bahía", TRUE),
  ("E-04", "Elevador 02", "Elevador", FALSE);

INSERT INTO repuesto (codigo, nombre, precio, cantidad_disponible, activo) VALUES
  ("REP001", "Pastillas de freno delanteras", 32000.00, 12, TRUE),
  ("REP002", "Filtro de aceite", 8500.00, 20, TRUE),
  ("REP003", "Aceite de motor 5W-30 (litro)", 6500.00, 40, TRUE),
  ("REP004", "Bujía de iridio", 9800.00, 16, TRUE),
  ("REP005", "Filtro de aire", 11000.00, 3, TRUE);

/*Usuarios del personal: {noop} le indica a Spring Security que la clave no esta encriptada*/
INSERT INTO usuario (username, password, nombre, activo, id_rol) VALUES
  ("admin", "{noop}Admin1", "Administrador del taller", TRUE, 1),
  ("recepcion", "{noop}Recep1", "Laura Mora Solano", TRUE, 2),
  ("mecanico1", "{noop}Meca1", "Carlos Vargas Rojas", TRUE, 3),
  ("mecanico2", "{noop}Meca2", "Diego Jiménez Castro", TRUE, 3);

INSERT INTO cliente (identificacion, nombre, telefono, correo) VALUES
  ("1-1111-1111", "Ana Quesada Ramírez", "8888-1111", "ana.quesada@correo.com"),
  ("2-2222-2222", "Mario Solís Chaves", "8888-2222", NULL),
  ("3-3333-3333", "Sofía Araya León", "8888-3333", "sofia.araya@correo.com");

INSERT INTO vehiculo (placa, marca, modelo, anio, id_cliente) VALUES
  ("BCD123", "Toyota", "Corolla", 2018, 1),
  ("FGH456", "Hyundai", "Tucson", 2021, 1),
  ("JKL789", "Nissan", "Sentra", 2016, 2),
  ("MNP321", "Suzuki", "Swift", 2020, 3);

INSERT INTO reserva (fecha, hora_inicio, hora_fin, motivo, estado, id_cliente, id_vehiculo, id_espacio) VALUES
  (CURDATE() + INTERVAL 1 DAY, "08:00", "10:00", "Cambio de aceite y filtros", "Confirmada", 1, 1, 1),
  (CURDATE() + INTERVAL 1 DAY, "10:00", "12:00", "Revisión de frenos", "Confirmada", 2, 3, 2);

INSERT INTO recepcion (fecha_ingreso, kilometraje, nivel_combustible, danos_visibles, objetos_personales, falla_reportada, id_vehiculo, id_reserva) VALUES
  (NOW() - INTERVAL 1 DAY, 85400, "1/2", NULL, NULL, "Ruido al frenar", 2, NULL),
  (NOW(), 42100, "3/4", "Rayón en puerta trasera", "Silla de bebé", "Luz de motor encendida", 4, NULL);

INSERT INTO diagnostico (fecha, fallas_encontradas, trabajos_necesarios, id_recepcion, id_mecanico) VALUES
  (NOW() - INTERVAL 1 DAY, "Pastillas delanteras desgastadas", "Cambio de pastillas delanteras", 1, 3);

INSERT INTO orden_servicio (fecha, estado, fecha_estado, total, id_diagnostico, id_mecanico) VALUES
  (NOW() - INTERVAL 1 DAY, "En proceso", NOW(), 25000.00, 1, 3);

INSERT INTO detalle_orden_servicio (descripcion, cantidad, costo, id_orden) VALUES
  ("Mano de obra cambio de pastillas", 1, 25000.00, 1);
  
  
  /*--- Pruebas --- */
SELECT username, password FROM autoflow.usuario;  
SELECT * FROM autoflow.reserva;