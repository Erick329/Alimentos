-- =====================================================================
-- Base de datos AlimentosSA - Proyecto Final Programación II (UMG Portales)
-- Script del grupo (corregido) + datos de prueba. Ejecutar completo en MySQL Workbench.
-- La tabla Historial no se incluye: el ingeniero indicó que no es necesaria para el alcance del proyecto.
-- =====================================================================

-- [CAMBIO] Mientras están desarrollando, se borra y se vuelve a crear para poder
-- ejecutar el script las veces que quieran sin errores de "la tabla ya existe".
set names utf8mb4;                                                     -- [CAMBIO] conexión en UTF-8 (tildes)
drop database if exists AlimentosSA;
create database AlimentosSA character set utf8mb4 collate utf8mb4_spanish_ci;  -- [CAMBIO] tildes y ñ
use AlimentosSA;

create table Roles(
idRol int auto_increment primary key,
nombre varchar(100) not null unique                                   -- [CAMBIO] unique
);

create table Categorias (
idCategoria int auto_increment primary key,
nombre varchar(100) not null unique,                                  -- [CAMBIO] unique
descripcion varchar(255) not null,
status varchar(20) not null default 'ACTIVO',
check (status in ('ACTIVO', 'INACTIVO'))                              -- [CAMBIO] solo valores válidos
);

create table Usuarios (
idUsuario int auto_increment primary key,
nombre varchar(100) not null,
apellido varchar(100) not null,
email varchar(255) not null unique,                                   -- [CAMBIO] no se repiten correos
nit varchar(20) not null default 'CF',                                -- [CAMBIO] consumidor final por defecto
password varchar(255) not null,                                       -- hash BCrypt, nunca texto plano
telefono varchar(20),                                                 -- [CAMBIO] faltaba (está en el diagrama)
direccion varchar(255) not null,
status varchar(20) not null default 'ACTIVO',
fechaRegistro datetime default current_timestamp,
intentosFallidos int not null default 0,                              -- [CAMBIO] nombre con "s" y valor inicial 0
idRol int not null,
foreign key(idRol) references Roles(idRol),
check (status in ('ACTIVO', 'INACTIVO'))
);

create table Productos(
idProducto int auto_increment primary key,
nombre varchar(100) not null,
descripcion varchar(255) not null,
codigo varchar(50) not null unique,                                   -- [CAMBIO] unique
precio decimal(10, 2) not null,
stock int not null default 0,                                         -- [CAMBIO] lo calculan los triggers de Lotes
stockMinimo int not null default 5,
status varchar(20) not null default 'ACTIVO',                         -- [CAMBIO] faltaba (está en el diagrama)
idCategoria int not null,
foreign key(idCategoria) references Categorias(idCategoria),
check (precio >= 0),                                                  -- [CAMBIO] no se permiten negativos
check (stock >= 0),
check (stockMinimo >= 0),
check (status in ('ACTIVO', 'INACTIVO'))
);

create table Lotes(
idLote int auto_increment primary key,
numeroLote varchar(50) not null,
cantidad int not null,
fechaVencimiento date not null,                                       -- [CAMBIO] DATE y SIN valor por defecto
fechaIngreso datetime default current_timestamp,
status varchar(20) not null default 'ACTIVO',
idProducto int not null,
foreign key(idProducto) references Productos(idProducto),
check (cantidad >= 0),                                                -- [CAMBIO]
check (status in ('ACTIVO', 'INACTIVO'))
);

create table Movimientos(
idMovimiento int auto_increment primary key,
tipo varchar(20) not null,
cantidad int not null,
motivo varchar(150),                                                  -- [CAMBIO] faltaba (está en el diagrama)
fecha datetime default current_timestamp,
idProducto int not null,
idUsuario int not null,
idLote int null,                                                      -- [CAMBIO] opcional
foreign key(idProducto) references Productos(idProducto),
foreign key(idUsuario) references Usuarios(idUsuario),
foreign key(idLote) references Lotes(idLote),
check (tipo in ('ENTRADA', 'SALIDA', 'AJUSTE'))                       -- [CAMBIO]
);

create table Pedidos(
idPedido int auto_increment primary key,
total decimal(10, 2) not null,
direccion varchar(255) not null,
costoEnvio decimal(10, 2) not null default 0,
fechaPedido datetime default current_timestamp,
status varchar(20) not null default 'PENDIENTE',
subTotal decimal(10, 2) not null,
iva decimal(10, 2) not null,
idUsuario int not null,
foreign key(idUsuario) references Usuarios(idUsuario),
check (status in ('PENDIENTE', 'PAGADO', 'EN_PREPARACION', 'ENVIADO', 'ENTREGADO', 'CANCELADO'))  -- [CAMBIO]
);

create table DetallePedido(
idDetallePedido int auto_increment primary key,
cantidad int not null,
precioUnitario decimal(10, 2) not null,
subTotal decimal(10, 2) not null,
idProducto int not null,
idPedido int not null,
foreign key(idProducto) references Productos(idProducto),
foreign key(idPedido) references Pedidos(idPedido),
check (cantidad > 0)                                                  -- [CAMBIO]
);

create table Pagos(
idPago int auto_increment primary key,
metodoPago varchar(30) not null,                                      -- [CAMBIO] not null
monto decimal(10, 2) not null,
status varchar(20) not null default 'PENDIENTE',
fechaPago datetime default current_timestamp,
numeroReferencia varchar(100) not null,
idPedido int not null,
foreign key(idPedido) references Pedidos(idPedido),
check (metodoPago in ('TARJETA', 'TRANSFERENCIA', 'EFECTIVO')),       -- [CAMBIO]
check (status in ('PENDIENTE', 'APROBADO', 'RECHAZADO'))              -- [CAMBIO]
);

-- =====================================================================
-- [CAMBIO] TRIGGERS: Productos.stock = suma de la cantidad de sus lotes ACTIVOS
-- =====================================================================
delimiter //

create trigger trg_lotes_insert after insert on Lotes for each row
begin
    update Productos
    set stock = (select coalesce(sum(cantidad), 0) from Lotes
                 where idProducto = new.idProducto and status = 'ACTIVO')
    where idProducto = new.idProducto;
end //

create trigger trg_lotes_update after update on Lotes for each row
begin
    update Productos
    set stock = (select coalesce(sum(cantidad), 0) from Lotes
                 where idProducto = new.idProducto and status = 'ACTIVO')
    where idProducto = new.idProducto;
end //

create trigger trg_lotes_delete after delete on Lotes for each row
begin
    update Productos
    set stock = (select coalesce(sum(cantidad), 0) from Lotes
                 where idProducto = old.idProducto and status = 'ACTIVO')
    where idProducto = old.idProducto;
end //

delimiter ;

-- =====================================================================
-- DATOS DE PRUEBA
-- Contraseñas (cifradas con BCrypt):
--   admin@alimentossa.com    -> Admin123*
--   vendedor@alimentossa.com -> Vendedor123*
--   cliente@correo.com (y demás clientes) -> Cliente123*
-- =====================================================================
insert into Roles (nombre) values ('ADMIN'), ('VENDEDOR'), ('CLIENTE');

insert into Usuarios (nombre, apellido, email, nit, password, telefono, direccion, idRol, fechaRegistro) values
('Admin', 'Sistema', 'admin@alimentossa.com', 'CF', '$2a$12$5OFX.nK50RGocspboZjy.utYqRTl3c1ytRHWBIsjO14M8PCgeHnKq', '5555-0000', 'Oficinas centrales, Portales', 1, '2026-06-15 08:00:00'),
('Valeria', 'Morales', 'vendedor@alimentossa.com', 'CF', '$2a$12$vyKPI/5ygrowaVqYl/9V4eia8bpcahZdo92wZIQjrZwyLsNtEA.jy', '5555-0001', 'Portales', 2, '2026-06-15 08:30:00'),
('Ana', 'López', 'cliente@correo.com', '1234567-8', '$2a$12$a.6RhHfuP.LoAp9XPfRigebnZyISWQBQlQwT/KGWwvj/chNrj7Zfu', '5512-3456', '3a. calle 4-12 zona 1, Portales', 3, '2026-06-20 10:15:00'),
('María', 'Pérez', 'maria.perez@correo.com', '2345678-9', '$2a$12$a.6RhHfuP.LoAp9XPfRigebnZyISWQBQlQwT/KGWwvj/chNrj7Zfu', '5523-4567', 'Lote 8, colonia Los Pinos, Portales', 3, '2026-06-25 16:40:00'),
('Carlos', 'Gómez', 'carlos.gomez@correo.com', 'CF', '$2a$12$a.6RhHfuP.LoAp9XPfRigebnZyISWQBQlQwT/KGWwvj/chNrj7Zfu', '5534-5678', 'Km 18 carretera al Atlántico, casa 5', 3, '2026-07-02 09:05:00'),
('Luis', 'Hernández', 'luis.hernandez@correo.com', '3456789-0', '$2a$12$a.6RhHfuP.LoAp9XPfRigebnZyISWQBQlQwT/KGWwvj/chNrj7Zfu', '5545-6789', '5a. avenida 2-30 zona 2, Palencia', 3, '2026-07-10 19:22:00'),
('Sofía', 'Ramírez', 'sofia.ramirez@correo.com', 'CF', '$2a$12$a.6RhHfuP.LoAp9XPfRigebnZyISWQBQlQwT/KGWwvj/chNrj7Zfu', '5556-7890', 'Residenciales Portales, manzana C casa 14', 3, '2026-08-01 11:48:00');

insert into Categorias (nombre, descripcion) values
('Granos básicos', 'Arroz, frijol, maíz y derivados'),
('Lácteos', 'Leche, quesos, crema y yogur'),
('Bebidas', 'Jugos, aguas, café y refrescos'),
('Snacks', 'Galletas, nueces y dulces'),
('Frutas y verduras', 'Productos frescos de temporada');

-- El stock se deja en 0: lo calculan los triggers al insertar los lotes
insert into Productos (nombre, descripcion, codigo, precio, stockMinimo, idCategoria) values
('Arroz blanco 5 lb', 'Arroz grano largo, calidad superior.', 'GB-001', 32.50, 20, 1),
('Frijol negro 2 lb', 'Frijol negro seleccionado y limpio.', 'GB-002', 18.00, 15, 1),
('Harina de maíz 2 lb', 'Harina de maíz nixtamalizado para tortillas.', 'GB-003', 9.75, 20, 1),
('Avena en hojuelas 1 lb', 'Avena integral en hojuelas.', 'GB-004', 11.50, 15, 1),
('Leche entera 1 L', 'Leche pasteurizada de vaca.', 'LA-001', 13.75, 10, 2),
('Queso fresco 1 lb', 'Queso fresco artesanal de la región.', 'LA-002', 28.00, 8, 2),
('Yogur natural 1 L', 'Yogur natural sin azúcar añadida.', 'LA-003', 24.50, 8, 2),
('Crema pura 400 ml', 'Crema pura para acompañar sus comidas.', 'LA-004', 16.00, 8, 2),
('Jugo de naranja 1 L', 'Jugo 100 % natural sin azúcar.', 'BE-001', 17.50, 10, 3),
('Agua pura 3 L', 'Agua purificada.', 'BE-002', 8.50, 24, 3),
('Café molido 400 g', 'Café de altura de Guatemala, tostado medio.', 'BE-003', 42.00, 10, 3),
('Refresco de rosa de Jamaica 1 L', 'Bebida natural de flor de Jamaica.', 'BE-004', 12.00, 10, 3),
('Galletas integrales', 'Paquete de 12 unidades.', 'SN-001', 12.00, 15, 4),
('Mix de nueces 200 g', 'Nueces, almendras y pasas.', 'SN-002', 35.00, 6, 4),
('Chocolate de mesa 4 tabletas', 'Chocolate artesanal para bebida.', 'SN-003', 22.00, 8, 4),
('Aguacate Hass (unidad)', 'Aguacate de temporada, listo para consumir.', 'FV-001', 6.50, 20, 5),
('Banano (docena)', 'Banano maduro de la costa sur.', 'FV-002', 10.00, 15, 5),
('Tomate 1 lb', 'Tomate fresco de invernadero.', 'FV-003', 5.50, 20, 5),
('Güisquil (unidad)', 'Güisquil tierno.', 'FV-004', 3.00, 15, 5),
('Papa 5 lb', 'Papa blanca lavada.', 'FV-005', 24.00, 10, 5);

insert into Lotes (numeroLote, cantidad, fechaVencimiento, fechaIngreso, idProducto) values
('A-1001', 60, '2027-06-30', '2026-07-01 08:00:00', 1),
('A-1002', 36, '2027-09-30', '2026-07-01 08:00:00', 1),
('F-2001', 64, '2027-03-31', '2026-07-01 08:00:00', 2),
('H-2101', 50, '2027-01-31', '2026-07-01 08:00:00', 3),
('H-2102', 30, '2027-02-28', '2026-07-01 08:00:00', 3),
('O-2201', 12, '2027-02-28', '2026-07-01 08:00:00', 4),
('L-3001', 15, '2026-10-20', '2026-08-26 08:00:00', 5),
('L-3002', 30, '2026-11-05', '2026-08-17 08:00:00', 5),
('Q-4001', 3, '2026-10-04', '2026-06-22 08:00:00', 6),
('Q-4002', 4, '2026-10-15', '2026-07-02 08:00:00', 6),
('Y-4101', 22, '2026-10-25', '2026-09-02 08:00:00', 7),
('C-4201', 30, '2026-10-30', '2026-08-23 08:00:00', 8),
('J-5001', 7, '2026-11-15', '2026-07-22 08:00:00', 9),
('W-5101', 120, '2027-09-01', '2026-07-01 08:00:00', 10),
('C-6001', 35, '2027-05-31', '2026-07-01 08:00:00', 11),
('R-6101', 40, '2026-12-15', '2026-08-18 08:00:00', 12),
('G-7001', 70, '2027-01-15', '2026-07-01 08:00:00', 13),
('N-7101', 18, '2027-02-10', '2026-07-01 08:00:00', 14),
('T-7201', 26, '2027-04-30', '2026-07-01 08:00:00', 15),
('V-8001', 90, '2026-10-18', '2026-06-29 08:00:00', 16),
('V-8101', 60, '2026-10-14', '2026-07-13 08:00:00', 17),
('V-8201', 75, '2026-10-16', '2026-06-25 08:00:00', 18),
('V-8301', 50, '2026-10-21', '2026-07-03 08:00:00', 19),
('V-8401', 38, '2026-11-10', '2026-07-31 08:00:00', 20);

-- Cada lote también queda registrado como ENTRADA en Movimientos
insert into Movimientos (tipo, cantidad, motivo, fecha, idProducto, idUsuario, idLote)
select 'ENTRADA', cantidad, 'Inventario inicial', fechaIngreso, idProducto, 1, idLote from Lotes;

insert into Pedidos (total, direccion, costoEnvio, fechaPedido, status, subTotal, iva, idUsuario) values
(215.00, '3a. calle 4-12 zona 1, Portales', 0.00, '2026-07-03 17:50:25', 'ENTREGADO', 215.00, 23.04, 3),
(54.00, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-07-03 19:19:45', 'CANCELADO', 29.00, 3.11, 4),
(118.00, '5a. avenida 2-30 zona 2, Palencia', 25.00, '2026-07-08 18:26:09', 'ENTREGADO', 93.00, 9.96, 6),
(177.00, '5a. avenida 2-30 zona 2, Palencia', 25.00, '2026-07-12 02:54:07', 'CANCELADO', 152.00, 16.29, 6),
(227.00, '3a. calle 4-12 zona 1, Portales', 0.00, '2026-07-12 16:15:19', 'CANCELADO', 227.00, 24.32, 3),
(83.50, 'Km 18 carretera al Atlántico, casa 5', 25.00, '2026-07-13 21:35:57', 'ENTREGADO', 58.50, 6.27, 5),
(134.50, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-07-21 19:30:18', 'ENTREGADO', 109.50, 11.73, 3),
(126.00, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-07-23 04:56:20', 'ENTREGADO', 101.00, 10.82, 4),
(122.00, '5a. avenida 2-30 zona 2, Palencia', 25.00, '2026-07-24 16:57:57', 'ENTREGADO', 97.00, 10.39, 6),
(244.00, 'Km 18 carretera al Atlántico, casa 5', 0.00, '2026-07-25 20:02:15', 'ENTREGADO', 244.00, 26.14, 5),
(118.50, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-07-26 12:42:30', 'ENTREGADO', 93.50, 10.02, 3),
(215.00, 'Residenciales Portales, manzana C casa 14', 25.00, '2026-07-29 18:58:51', 'ENTREGADO', 190.00, 20.36, 7),
(176.50, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-08-03 11:50:33', 'ENTREGADO', 151.50, 16.23, 3),
(59.50, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-08-05 17:57:42', 'ENTREGADO', 34.50, 3.70, 3),
(209.25, '3a. calle 4-12 zona 1, Portales', 0.00, '2026-08-09 04:19:55', 'CANCELADO', 209.25, 22.42, 3),
(213.00, 'Km 18 carretera al Atlántico, casa 5', 0.00, '2026-08-11 05:52:51', 'ENTREGADO', 213.00, 22.82, 5),
(79.50, 'Residenciales Portales, manzana C casa 14', 25.00, '2026-08-11 11:08:40', 'ENTREGADO', 54.50, 5.84, 7),
(262.50, 'Km 18 carretera al Atlántico, casa 5', 0.00, '2026-08-14 03:17:34', 'ENTREGADO', 262.50, 28.12, 5),
(77.00, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-08-14 21:30:46', 'ENTREGADO', 52.00, 5.57, 4),
(261.00, 'Residenciales Portales, manzana C casa 14', 0.00, '2026-08-17 00:20:46', 'ENTREGADO', 261.00, 27.96, 7),
(172.00, 'Km 18 carretera al Atlántico, casa 5', 25.00, '2026-08-18 04:01:47', 'ENTREGADO', 147.00, 15.75, 5),
(178.00, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-08-18 21:23:16', 'CANCELADO', 153.00, 16.39, 4),
(140.25, 'Residenciales Portales, manzana C casa 14', 25.00, '2026-08-19 15:52:00', 'ENTREGADO', 115.25, 12.35, 7),
(131.00, 'Km 18 carretera al Atlántico, casa 5', 25.00, '2026-08-20 08:46:03', 'ENTREGADO', 106.00, 11.36, 5),
(200.75, '3a. calle 4-12 zona 1, Portales', 0.00, '2026-08-20 19:03:14', 'ENTREGADO', 200.75, 21.51, 3),
(185.50, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-08-24 15:19:09', 'ENTREGADO', 160.50, 17.20, 4),
(210.00, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-08-26 14:00:56', 'ENTREGADO', 185.00, 19.82, 4),
(123.00, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-08-27 09:24:29', 'ENTREGADO', 98.00, 10.50, 4),
(207.50, 'Km 18 carretera al Atlántico, casa 5', 25.00, '2026-08-27 11:17:08', 'ENTREGADO', 182.50, 19.55, 5),
(144.00, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-08-28 19:11:10', 'ENTREGADO', 119.00, 12.75, 3),
(74.50, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-08-30 04:00:34', 'ENTREGADO', 49.50, 5.30, 4),
(257.50, '3a. calle 4-12 zona 1, Portales', 0.00, '2026-09-05 12:24:40', 'ENTREGADO', 257.50, 27.59, 3),
(108.00, '5a. avenida 2-30 zona 2, Palencia', 25.00, '2026-09-06 01:56:46', 'ENTREGADO', 83.00, 8.89, 6),
(110.00, 'Lote 8, colonia Los Pinos, Portales', 25.00, '2026-09-07 21:53:41', 'ENTREGADO', 85.00, 9.11, 4),
(144.00, 'Residenciales Portales, manzana C casa 14', 25.00, '2026-09-08 13:59:46', 'ENTREGADO', 119.00, 12.75, 7),
(219.00, '3a. calle 4-12 zona 1, Portales', 0.00, '2026-09-12 09:50:35', 'ENTREGADO', 219.00, 23.46, 3),
(85.75, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-09-12 11:53:55', 'ENTREGADO', 60.75, 6.51, 3),
(102.00, '5a. avenida 2-30 zona 2, Palencia', 25.00, '2026-09-17 18:24:27', 'ENTREGADO', 77.00, 8.25, 6),
(138.75, 'Residenciales Portales, manzana C casa 14', 25.00, '2026-09-18 16:54:54', 'ENTREGADO', 113.75, 12.19, 7),
(163.00, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-09-18 21:35:16', 'ENTREGADO', 138.00, 14.79, 3),
(209.50, 'Km 18 carretera al Atlántico, casa 5', 0.00, '2026-09-24 17:11:53', 'ENTREGADO', 209.50, 22.45, 5),
(86.00, '5a. avenida 2-30 zona 2, Palencia', 25.00, '2026-09-28 15:22:20', 'ENVIADO', 61.00, 6.54, 6),
(109.50, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-09-29 13:31:58', 'ENTREGADO', 84.50, 9.05, 3),
(128.50, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-09-29 20:44:37', 'ENTREGADO', 103.50, 11.09, 3),
(66.00, '3a. calle 4-12 zona 1, Portales', 25.00, '2026-10-04 19:02:06', 'PAGADO', 41.00, 4.39, 3),
(174.50, '5a. avenida 2-30 zona 2, Palencia', 25.00, '2026-10-06 17:11:45', 'PAGADO', 149.50, 16.02, 6);

insert into DetallePedido (cantidad, precioUnitario, subTotal, idProducto, idPedido) values
(4, 17.50, 70.00, 9, 1),
(4, 10.00, 40.00, 17, 1),
(3, 35.00, 105.00, 14, 1),
(1, 16.00, 16.00, 8, 2),
(2, 6.50, 13.00, 16, 2),
(3, 5.50, 16.50, 18, 3),
(3, 12.00, 36.00, 13, 3),
(4, 8.50, 34.00, 10, 3),
(1, 6.50, 6.50, 16, 3),
(1, 32.50, 32.50, 1, 4),
(1, 5.50, 5.50, 18, 4),
(4, 24.00, 96.00, 20, 4),
(1, 18.00, 18.00, 2, 4),
(1, 18.00, 18.00, 2, 5),
(4, 42.00, 168.00, 11, 5),
(2, 12.00, 24.00, 13, 5),
(2, 8.50, 17.00, 10, 5),
(4, 8.50, 34.00, 10, 6),
(1, 24.50, 24.50, 7, 6),
(4, 8.50, 34.00, 10, 7),
(4, 12.00, 48.00, 13, 7),
(2, 13.75, 27.50, 5, 7),
(2, 8.50, 17.00, 10, 8),
(2, 42.00, 84.00, 11, 8),
(2, 17.50, 35.00, 9, 9),
(2, 18.00, 36.00, 2, 9),
(4, 6.50, 26.00, 16, 9),
(3, 17.50, 52.50, 9, 10),
(4, 13.75, 55.00, 5, 10),
(4, 28.00, 112.00, 6, 10),
(1, 24.50, 24.50, 7, 10),
(1, 10.00, 10.00, 17, 11),
(3, 11.50, 34.50, 4, 11),
(2, 24.50, 49.00, 7, 11),
(1, 18.00, 18.00, 2, 12),
(3, 42.00, 126.00, 11, 12),
(1, 12.00, 12.00, 13, 12),
(4, 8.50, 34.00, 10, 12),
(3, 8.50, 25.50, 10, 13),
(3, 42.00, 126.00, 11, 13),
(1, 10.00, 10.00, 17, 14),
(1, 24.50, 24.50, 7, 14),
(4, 42.00, 168.00, 11, 15),
(3, 13.75, 41.25, 5, 15),
(3, 32.50, 97.50, 1, 16),
(3, 10.00, 30.00, 17, 16),
(1, 12.00, 12.00, 13, 16),
(3, 24.50, 73.50, 7, 16),
(2, 9.75, 19.50, 3, 17),
(1, 35.00, 35.00, 14, 17),
(2, 32.50, 65.00, 1, 18),
(1, 12.00, 12.00, 13, 18),
(4, 28.00, 112.00, 6, 18),
(3, 24.50, 73.50, 7, 18),
(2, 18.00, 36.00, 2, 19),
(1, 16.00, 16.00, 8, 19),
(2, 32.50, 65.00, 1, 20),
(4, 17.50, 70.00, 9, 20),
(3, 42.00, 126.00, 11, 20),
(3, 18.00, 54.00, 2, 21),
(4, 9.75, 39.00, 3, 21),
(1, 24.50, 24.50, 7, 21),
(2, 12.00, 24.00, 12, 21),
(1, 5.50, 5.50, 18, 21),
(2, 24.00, 48.00, 20, 22),
(3, 35.00, 105.00, 14, 22),
(2, 32.50, 65.00, 1, 23),
(3, 3.00, 9.00, 19, 23),
(3, 13.75, 41.25, 5, 23),
(1, 10.00, 10.00, 17, 24),
(2, 5.50, 11.00, 18, 24),
(3, 24.00, 72.00, 20, 24),
(2, 6.50, 13.00, 16, 24),
(2, 32.50, 65.00, 1, 25),
(1, 9.75, 9.75, 3, 25),
(3, 42.00, 126.00, 11, 25),
(3, 17.50, 52.50, 9, 26),
(4, 18.00, 72.00, 2, 26),
(3, 12.00, 36.00, 12, 26),
(2, 18.00, 36.00, 2, 27),
(4, 13.75, 55.00, 5, 27),
(2, 24.50, 49.00, 7, 27),
(3, 8.50, 25.50, 10, 27),
(3, 6.50, 19.50, 16, 27),
(2, 17.50, 35.00, 9, 28),
(3, 6.50, 19.50, 16, 28),
(2, 13.75, 27.50, 5, 28),
(1, 16.00, 16.00, 8, 28),
(4, 32.50, 130.00, 1, 29),
(3, 5.50, 16.50, 18, 29),
(3, 12.00, 36.00, 13, 29),
(2, 18.00, 36.00, 2, 30),
(1, 28.00, 28.00, 6, 30),
(3, 8.50, 25.50, 10, 30),
(3, 6.50, 19.50, 16, 30),
(1, 10.00, 10.00, 17, 30),
(1, 32.50, 32.50, 1, 31),
(2, 8.50, 17.00, 10, 31),
(2, 32.50, 65.00, 1, 32),
(2, 18.00, 36.00, 2, 32),
(4, 35.00, 140.00, 14, 32),
(3, 5.50, 16.50, 18, 32),
(4, 17.50, 70.00, 9, 33),
(2, 6.50, 13.00, 16, 33),
(4, 8.50, 34.00, 10, 34),
(1, 9.75, 9.75, 3, 34),
(3, 13.75, 41.25, 5, 34),
(2, 17.50, 35.00, 9, 35),
(3, 28.00, 84.00, 6, 35),
(3, 32.50, 97.50, 1, 36),
(4, 18.00, 72.00, 2, 36),
(2, 9.75, 19.50, 3, 36),
(2, 8.50, 17.00, 10, 36),
(2, 6.50, 13.00, 16, 36),
(2, 5.50, 11.00, 18, 37),
(1, 13.75, 13.75, 5, 37),
(2, 18.00, 36.00, 2, 37),
(4, 5.50, 22.00, 18, 38),
(4, 13.75, 55.00, 5, 38),
(2, 17.50, 35.00, 9, 39),
(2, 32.50, 65.00, 1, 39),
(1, 13.75, 13.75, 5, 39),
(3, 10.00, 30.00, 17, 40),
(4, 8.50, 34.00, 10, 40),
(4, 9.75, 39.00, 3, 40),
(1, 35.00, 35.00, 14, 40),
(3, 32.50, 97.50, 1, 41),
(3, 18.00, 54.00, 2, 41),
(3, 12.00, 36.00, 12, 41),
(4, 5.50, 22.00, 18, 41),
(2, 24.00, 48.00, 20, 42),
(2, 6.50, 13.00, 16, 42),
(3, 17.50, 52.50, 9, 43),
(3, 8.50, 25.50, 10, 43),
(1, 6.50, 6.50, 16, 43),
(2, 42.00, 84.00, 11, 44),
(3, 6.50, 19.50, 16, 44),
(1, 32.50, 32.50, 1, 45),
(1, 8.50, 8.50, 10, 45),
(4, 11.50, 46.00, 4, 46),
(2, 13.75, 27.50, 5, 46),
(1, 12.00, 12.00, 13, 46),
(4, 16.00, 64.00, 8, 46);

insert into Pagos (metodoPago, monto, status, fechaPago, numeroReferencia, idPedido) values
('TRANSFERENCIA', 215.00, 'APROBADO', '2026-07-03 17:50:25', 'SIM-99892850', 1),
('TRANSFERENCIA', 54.00, 'RECHAZADO', '2026-07-03 19:19:45', 'SIM-80180947', 2),
('EFECTIVO', 118.00, 'APROBADO', '2026-07-08 18:26:09', 'SIM-44739729', 3),
('TARJETA', 177.00, 'RECHAZADO', '2026-07-12 02:54:07', 'SIM-43264201 ****1111', 4),
('TARJETA', 227.00, 'RECHAZADO', '2026-07-12 16:15:19', 'SIM-78142594 ****4242', 5),
('TARJETA', 83.50, 'APROBADO', '2026-07-13 21:35:57', 'SIM-96741219 ****4242', 6),
('EFECTIVO', 134.50, 'APROBADO', '2026-07-21 19:30:18', 'SIM-79741374', 7),
('EFECTIVO', 126.00, 'APROBADO', '2026-07-23 04:56:20', 'SIM-16467611', 8),
('TRANSFERENCIA', 122.00, 'APROBADO', '2026-07-24 16:57:57', 'SIM-81539513', 9),
('EFECTIVO', 244.00, 'APROBADO', '2026-07-25 20:02:15', 'SIM-46753198', 10),
('EFECTIVO', 118.50, 'APROBADO', '2026-07-26 12:42:30', 'SIM-86817133', 11),
('TARJETA', 215.00, 'APROBADO', '2026-07-29 18:58:51', 'SIM-18110255 ****4242', 12),
('TARJETA', 176.50, 'APROBADO', '2026-08-03 11:50:33', 'SIM-44376392 ****1111', 13),
('TARJETA', 59.50, 'APROBADO', '2026-08-05 17:57:42', 'SIM-51596580 ****1111', 14),
('TARJETA', 209.25, 'RECHAZADO', '2026-08-09 04:19:55', 'SIM-89733040 ****4242', 15),
('TARJETA', 213.00, 'APROBADO', '2026-08-11 05:52:51', 'SIM-88598717 ****1111', 16),
('TARJETA', 79.50, 'APROBADO', '2026-08-11 11:08:40', 'SIM-26868746 ****5454', 17),
('TARJETA', 262.50, 'APROBADO', '2026-08-14 03:17:34', 'SIM-13135594 ****1111', 18),
('TARJETA', 77.00, 'APROBADO', '2026-08-14 21:30:46', 'SIM-55729726 ****5454', 19),
('EFECTIVO', 261.00, 'APROBADO', '2026-08-17 00:20:46', 'SIM-47012142', 20),
('TARJETA', 172.00, 'APROBADO', '2026-08-18 04:01:47', 'SIM-83380123 ****5454', 21),
('TARJETA', 178.00, 'RECHAZADO', '2026-08-18 21:23:16', 'SIM-62784615 ****4242', 22),
('TARJETA', 140.25, 'APROBADO', '2026-08-19 15:52:00', 'SIM-57945702 ****1111', 23),
('TARJETA', 131.00, 'APROBADO', '2026-08-20 08:46:03', 'SIM-25662869 ****5454', 24),
('TARJETA', 200.75, 'APROBADO', '2026-08-20 19:03:14', 'SIM-19815488 ****4242', 25),
('TARJETA', 185.50, 'APROBADO', '2026-08-24 15:19:09', 'SIM-21793294 ****4242', 26),
('EFECTIVO', 210.00, 'APROBADO', '2026-08-26 14:00:56', 'SIM-61221192', 27),
('TARJETA', 123.00, 'APROBADO', '2026-08-27 09:24:29', 'SIM-87093109 ****4242', 28),
('TRANSFERENCIA', 207.50, 'APROBADO', '2026-08-27 11:17:08', 'SIM-33215571', 29),
('TRANSFERENCIA', 144.00, 'APROBADO', '2026-08-28 19:11:10', 'SIM-79114285', 30),
('TARJETA', 74.50, 'APROBADO', '2026-08-30 04:00:34', 'SIM-72205072 ****1111', 31),
('TARJETA', 257.50, 'APROBADO', '2026-09-05 12:24:40', 'SIM-52656849 ****5454', 32),
('TRANSFERENCIA', 108.00, 'APROBADO', '2026-09-06 01:56:46', 'SIM-59148845', 33),
('EFECTIVO', 110.00, 'APROBADO', '2026-09-07 21:53:41', 'SIM-22176004', 34),
('EFECTIVO', 144.00, 'APROBADO', '2026-09-08 13:59:46', 'SIM-61382379', 35),
('TARJETA', 219.00, 'APROBADO', '2026-09-12 09:50:35', 'SIM-98178629 ****5454', 36),
('TRANSFERENCIA', 85.75, 'APROBADO', '2026-09-12 11:53:55', 'SIM-96063778', 37),
('TARJETA', 102.00, 'APROBADO', '2026-09-17 18:24:27', 'SIM-29548259 ****5454', 38),
('TRANSFERENCIA', 138.75, 'APROBADO', '2026-09-18 16:54:54', 'SIM-36767803', 39),
('TARJETA', 163.00, 'APROBADO', '2026-09-18 21:35:16', 'SIM-25549507 ****1111', 40),
('TRANSFERENCIA', 209.50, 'APROBADO', '2026-09-24 17:11:53', 'SIM-79929784', 41),
('TRANSFERENCIA', 86.00, 'PENDIENTE', '2026-09-28 15:22:20', 'SIM-60654072', 42),
('EFECTIVO', 109.50, 'APROBADO', '2026-09-29 13:31:58', 'SIM-15620131', 43),
('TARJETA', 128.50, 'APROBADO', '2026-09-29 20:44:37', 'SIM-24113414 ****4242', 44),
('TARJETA', 66.00, 'APROBADO', '2026-10-04 19:02:06', 'SIM-30566570 ****4242', 45),
('TRANSFERENCIA', 174.50, 'PENDIENTE', '2026-10-06 17:11:45', 'SIM-74805280', 46);

-- Verificación: stock calculado por los triggers
select codigo, nombre, stock, stockMinimo from Productos;
