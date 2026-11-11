-- =====================================================================
-- Proyecto Final Bases de Datos I - Tours y Actividades
-- 02_datos.sql : catálogos y datos de prueba
--
-- Todas las cuentas de prueba (clientes y usuarios internos) usan la
-- contraseña: Clave123*   (guardada con BCrypt)
-- Los datos de prueba cumplen las reglas de negocio de la entrega
-- (idioma del guía, cupo, edad mínima, plan del mismo prestador, etc.)
-- =====================================================================
SET NAMES utf8mb4;
USE tours_actividades;

-- ---------------------------- CATÁLOGOS -----------------------------
INSERT INTO municipio (id_municipio, nombre, codigo_dane) VALUES
 (1,'Armenia','63001'), (2,'Buenavista','63111'), (3,'Calarcá','63130'),
 (4,'Circasia','63190'), (5,'Córdoba','63212'), (6,'Filandia','63272'),
 (7,'Génova','63302'), (8,'La Tebaida','63401'), (9,'Montenegro','63470'),
 (10,'Pijao','63548'), (11,'Quimbaya','63594'), (12,'Salento','63690');

INSERT INTO tipo_prestador (id_tipo_prestador, nombre_tipo) VALUES
 (1,'Alojamiento'), (2,'Agencia de viajes'), (3,'Transporte'), (4,'Gastronomía'),
 (5,'Guianza'), (6,'Alquiler de vehículos'), (7,'Atracción'), (8,'Otros');

INSERT INTO rol (id_rol, nombre) VALUES
 (1,'Administrador de la plataforma'), (2,'Encargado de prestador'), (3,'Operador');

INSERT INTO categoria_tour (id_categoria, nombre) VALUES
 (1,'Cultural'), (2,'Aventura'), (3,'Naturaleza'), (4,'Cafetero');

INSERT INTO idioma (id_idioma, nombre_idioma) VALUES
 (1,'Español'), (2,'Inglés'), (3,'Francés'), (4,'Alemán'), (5,'Portugués');

-- ---------------------------- PRESTADORES ---------------------------
INSERT INTO prestador (id_prestador, nombre_comercial, nit_documento, numero_rnt, direccion,
                       telefono, correo, estado, id_municipio, id_tipo_prestador) VALUES
 (1,'Cocora Expediciones SAS','900123456-1','45871','Calle 6 # 4-15, Salento','3104561230','contacto@cocoraexpediciones.co','ACTIVO',12,5),
 (2,'Aventura Quindío Tours','901234567-2','51234','Vía al mirador km 2, Buenavista','3157894560','reservas@aventuraquindio.co','ACTIVO',2,2),
 (3,'Ruta del Café Filandia','900987654-3','38765','Carrera 5 # 7-20, Filandia','3201237894','hola@rutadelcafe.co','ACTIVO',6,5),
 (4,'Hotel Mirador del Valle','901112223-4','60011','Avenida Bolívar # 14N-30, Armenia','3009871234','reservas@miradordelvalle.co','ACTIVO',1,1),
 (5,'Transportes Eje Turístico','900445566-5','29870','Calle 40 # 25-10, Calarcá','3186549870','info@ejeturistico.co','SUSPENDIDO',3,3),
 (6,'Senderos de Pijao','901778899-6','41122','Plaza principal, Pijao','3012345678','senderos@pijao.co','RETIRADO',10,5);

-- ------------------------------- PLANES -----------------------------
INSERT INTO plan (id_plan, nombre, descripcion, tipo, precio, fecha_inicio, fecha_fin, estado, id_prestador) VALUES
 (1,'Grupal Cocora','Tour compartido con otros viajeros, guía y seguro incluidos.','ESTANDAR',120000,'2026-01-01','2026-12-31','ACTIVO',1),
 (2,'Privado Cocora','Tour exclusivo para el grupo, con transporte desde Salento.','PREMIUM',300000,'2026-01-01','2026-12-31','ACTIVO',1),
 (3,'Aventura grupal','Actividad compartida con equipo e instructor certificado.','ESTANDAR',180000,'2026-01-01','2026-12-31','ACTIVO',2),
 (4,'Aventura privada','Actividad exclusiva con video y fotografías incluidas.','PREMIUM',350000,'2026-01-01','2026-12-31','ACTIVO',2),
 (5,'Ruta cafetera grupal','Recorrido grupal por finca con degustación.','BASICO',85000,'2026-01-01','2026-12-31','ACTIVO',3),
 (6,'Solo alojamiento','Habitación doble sin alimentación.','BASICO',250000,'2026-01-01','2026-12-31','ACTIVO',4),
 (7,'Todo incluido Mirador','Alojamiento, alimentación y actividades del hotel.','TODO_INCLUIDO',480000,'2026-01-01','2026-12-31','ACTIVO',4),
 (8,'Temporada 2025 Cocora','Plan de la temporada anterior.','BASICO',100000,'2025-01-01','2025-12-31','INACTIVO',1);

-- ---------------------- USUARIOS DEL SISTEMA ------------------------
INSERT INTO usuario_sistema (id_usuario, nombre_usuario, contrasena_cifrada, estado, id_rol, id_prestador) VALUES
 (1,'admin','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ACTIVO',1,NULL),
 (2,'cocora.encargado','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ACTIVO',2,1),
 (3,'aventura.encargado','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ACTIVO',2,2),
 (4,'cafe.encargado','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ACTIVO',2,3),
 (5,'mirador.encargado','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','BLOQUEADO',2,4),
 (6,'operador1','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ACTIVO',3,NULL);

-- ----------------------------- CLIENTES -----------------------------
INSERT INTO cliente (id_cliente, nombre, apellido, tipo_documento, numero_documento, correo,
                     telefono, fecha_nacimiento, ciudad_origen) VALUES
 (1,'Valentina','Rojas','CEDULA_CIUDADANIA','1094123456','valentina.rojas@gmail.com','3104567890','1998-04-12','Bogotá'),
 (2,'Mateo','Herrera','CEDULA_CIUDADANIA','1020456789','mateo.herrera@hotmail.com','3157894561','1990-09-03','Medellín'),
 (3,'Emma','Schneider','PASAPORTE','C4X82J91','emma.schneider@web.de','3201234567','1985-06-22','Múnich'),
 (4,'Lucas','Martin','PASAPORTE','19AB45632','lucas.martin@orange.fr','3009876543','1992-11-30','Lyon'),
 (5,'Camila','Duque','CEDULA_CIUDADANIA','1094987321','camila.duque@gmail.com','3186543210','2000-01-15','Armenia'),
 (6,'João','Silva','PASAPORTE','FZ123456','joao.silva@gmail.com','3112223344','1988-03-08','São Paulo'),
 (7,'Andrés','López','CEDULA_EXTRANJERIA','987654','andres.lopez@yahoo.com','3015556677','1979-07-19','Pereira');
-- El cliente 7 se registró en el mostrador: no tiene perfil.

INSERT INTO perfil_cliente (id_cliente, correo_cuenta, contrasena_cifrada, idioma_preferencia,
                            canal_notificacion_preferido, anticipacion_recordatorios, acepta_promociones) VALUES
 (1,'vrojas.app@gmail.com','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ESPANOL','SMS',1,TRUE),
 (2,'mherrera.app@hotmail.com','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ESPANOL','CORREO',3,FALSE),
 (3,'eschneider.app@web.de','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','INGLES','CORREO',7,TRUE),
 (4,'lmartin.app@orange.fr','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','INGLES','CORREO',7,TRUE),
 (5,'cduque.app@gmail.com','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','ESPANOL','WHATSAPP',3,TRUE),
 (6,'jsilva.app@gmail.com','$2b$10$LeiewBuoo0GFKmVbOf6bMem8bZ0UwXXeuBI/JE7G5rEwnaPZKC2q6','INGLES','WHATSAPP',1,FALSE);

-- ------------------------------- TOURS ------------------------------
INSERT INTO tour (id_tour, nombre, descripcion, duracion_horas, dificultad, edad_minima,
                  precio_persona, id_categoria, id_prestador) VALUES
 (1,'Caminata Valle de Cocora','Recorrido entre palmas de cera hasta Acaime, con hidratación incluida.',5.00,'MEDIA',8,120000,3,1),
 (2,'Avistamiento de aves en Cocora','Observación de aves endémicas con binoculares y guía especializado.',4.00,'BAJA',0,95000,3,1),
 (3,'Parapente en Buenavista','Vuelo biplaza sobre el valle del río Barragán con instructor certificado.',2.00,'ALTA',14,250000,2,2),
 (4,'Canopy y rappel Quindío','Circuito de canopy y descenso en rappel por cascada.',3.00,'ALTA',12,180000,2,2),
 (5,'Tour del café en finca tradicional','Del grano a la taza: recolección, beneficio y catación.',3.50,'BAJA',0,85000,4,3),
 (6,'Recorrido histórico por Filandia','Arquitectura de la colonización antioqueña y mirador colina iluminada.',2.50,'BAJA',0,60000,1,3),
 (7,'Salento colonial y artesanías','Calle Real, talleres artesanales y mirador de Salento.',3.00,'BAJA',0,70000,1,1);
-- El tour 6 no tiene salidas y el tour 7 tiene una salida sin participantes válidos (reporte 9).

-- ------------------------- GUÍAS E IDIOMAS --------------------------
INSERT INTO guia (id_guia, nombre, documento, tarjeta_profesional) VALUES
 (1,'Juan Pablo Ramírez','1094888111','TP-QUI-0101'),
 (2,'María Fernanda Ospina','1094777222','TP-QUI-0102'),
 (3,'Carlos Andrés Gómez','1097333444','TP-QUI-0103'),
 (4,'Laura Valentina Arias','1094555666','TP-QUI-0104'),
 (5,'Andrés Felipe Restrepo','1094999000','TP-QUI-0105');

INSERT INTO guia_idioma (id_guia, id_idioma) VALUES
 (1,1),(1,2),
 (2,1),(2,2),(2,3),
 (3,1),
 (4,1),(4,2),(4,4),
 (5,1),(5,5);

-- ----------------------------- SALIDAS ------------------------------
INSERT INTO salida_tour (id_salida, fecha_salida, hora_salida, cupo, estado, id_tour, id_guia, id_idioma) VALUES
 (1,'2026-01-17','07:00:00',15,'REALIZADA',1,1,2),
 (2,'2026-02-14','07:00:00',15,'REALIZADA',1,3,1),
 (3,'2026-02-21','09:00:00',6,'REALIZADA',3,4,4),
 (4,'2026-03-07','10:00:00',20,'REALIZADA',5,2,3),
 (5,'2026-03-21','08:00:00',10,'REALIZADA',4,5,5),
 (6,'2026-04-11','06:00:00',12,'REALIZADA',2,1,1),
 (7,'2026-05-16','07:00:00',15,'CANCELADA',1,3,1),
 (8,'2026-10-10','14:00:00',15,'REALIZADA',7,2,1),
 (9,'2026-11-21','07:00:00',15,'PROGRAMADA',1,1,1),
 (10,'2026-11-28','09:00:00',6,'PROGRAMADA',3,4,2),
 (11,'2026-12-05','10:00:00',20,'PROGRAMADA',5,2,2),
 (12,'2026-12-12','08:00:00',10,'PROGRAMADA',4,5,1);

-- ----------------------------- RESERVAS -----------------------------
-- valor_total = precio_persona del tour x número de participantes
INSERT INTO reserva (id_reserva, fecha_creacion, fecha_servicio, estado, valor_total,
                     fecha_cancelacion, canal, id_cliente, id_plan, id_salida) VALUES
 (1,'2026-01-05 10:15:00','2026-01-17','COMPLETADA',240000,NULL,'WEB',1,1,1),
 (2,'2026-01-10 18:00:00','2026-01-17','COMPLETADA',120000,NULL,'APP_MOVIL',6,1,1),
 (3,'2026-02-01 09:30:00','2026-02-14','COMPLETADA',360000,NULL,'WEB',2,1,2),
 (4,'2026-02-05 11:00:00','2026-02-14','CANCELADA',240000,'2026-02-06 08:00:00','TELEFONO',5,1,2),
 (5,'2026-02-10 15:20:00','2026-02-21','COMPLETADA',500000,NULL,'WEB',3,4,3),
 (6,'2026-02-25 10:00:00','2026-03-07','COMPLETADA',340000,NULL,'WEB',4,5,4),
 (7,'2026-03-01 13:00:00','2026-03-07','COMPLETADA',170000,NULL,'MOSTRADOR',7,5,4),
 (8,'2026-03-10 09:00:00','2026-03-21','COMPLETADA',360000,NULL,'APP_MOVIL',6,3,5),
 (9,'2026-04-01 16:00:00','2026-04-11','COMPLETADA',95000,NULL,'WEB',1,1,6),
 (10,'2026-04-20 10:00:00','2026-05-16','CANCELADA',240000,'2026-05-14 09:00:00','WEB',2,1,7),
 (11,'2026-09-28 12:00:00','2026-10-10','CANCELADA',70000,'2026-10-08 10:00:00','WEB',5,1,8),
 (12,'2026-10-25 09:00:00','2026-11-21','CONFIRMADA',240000,NULL,'WEB',5,1,9),
 (13,'2026-10-30 19:00:00','2026-11-28','PENDIENTE',250000,NULL,'APP_MOVIL',1,3,10),
 (14,'2026-11-02 08:30:00','2026-12-05','CONFIRMADA',255000,NULL,'WEB',4,5,11),
 (15,'2026-11-03 14:00:00','2026-12-12','PENDIENTE',360000,NULL,'TELEFONO',2,4,12);

-- -------------------------- PARTICIPANTES ---------------------------
INSERT INTO participante (id_reserva, num_participante, nombre, documento, fecha_nacimiento) VALUES
 (1,1,'Valentina Rojas','1094123456','1998-04-12'),
 (1,2,'Santiago Rojas','1094123999','1995-08-20'),
 (2,1,'João Silva','FZ123456','1988-03-08'),
 (3,1,'Mateo Herrera','1020456789','1990-09-03'),
 (3,2,'Sara Herrera','1020456790','2015-05-10'),
 (3,3,'Paula Gómez','1020999888','1991-02-14'),
 (4,1,'Camila Duque','1094987321','2000-01-15'),
 (4,2,'Daniel Duque','1094987322','2002-07-01'),
 (5,1,'Emma Schneider','C4X82J91','1985-06-22'),
 (5,2,'Jonas Schneider','C4X82J92','2010-03-15'),
 (6,1,'Lucas Martin','19AB45632','1992-11-30'),
 (6,2,'Chloé Martin','19AB45633','1993-04-02'),
 (6,3,'Léo Martin','21CD11223','2019-06-10'),
 (6,4,'Inès Martin','24EF33445','2024-01-20'),
 (7,1,'Andrés López','987654','1979-07-19'),
 (7,2,'Marta Ruiz','41922333','1981-12-05'),
 (8,1,'João Silva','FZ123456','1988-03-08'),
 (8,2,'Ana Silva','FZ654321','1990-10-10'),
 (9,1,'Valentina Rojas','1094123456','1998-04-12'),
 (10,1,'Mateo Herrera','1020456789','1990-09-03'),
 (10,2,'Paula Gómez','1020999888','1991-02-14'),
 (11,1,'Camila Duque','1094987321','2000-01-15'),
 (12,1,'Camila Duque','1094987321','2000-01-15'),
 (12,2,'Daniel Duque','1094987322','2002-07-01'),
 (13,1,'Valentina Rojas','1094123456','1998-04-12'),
 (14,1,'Lucas Martin','19AB45632','1992-11-30'),
 (14,2,'Chloé Martin','19AB45633','1993-04-02'),
 (14,3,'Léo Martin','21CD11223','2019-06-10'),
 (15,1,'Mateo Herrera','1020456789','1990-09-03'),
 (15,2,'Paula Gómez','1020999888','1991-02-14');

-- ------------------------------ PAGOS -------------------------------
INSERT INTO pago (id_pago, fecha, monto, metodo, estado, referencia_transaccion, id_reserva) VALUES
 (1,'2026-01-05 10:20:00',240000,'TARJETA_CREDITO','EXITOSO','TXN-2026-0001',1),
 (2,'2026-01-10 18:05:00',60000,'PSE','EXITOSO','PSE-2026-0002',2),
 (3,'2026-01-15 09:00:00',60000,'PSE','EXITOSO','PSE-2026-0003',2),
 (4,'2026-02-01 09:35:00',360000,'TARJETA_DEBITO','EXITOSO','TXN-2026-0004',3),
 (5,'2026-02-05 11:05:00',240000,'PSE','EXITOSO','PSE-2026-0005',4),
 (6,'2026-02-06 08:05:00',192000,'PSE','REEMBOLSADO','PSE-2026-0006',4),
 (7,'2026-02-10 15:25:00',500000,'TARJETA_CREDITO','EXITOSO','TXN-2026-0007',5),
 (8,'2026-02-25 10:05:00',340000,'TARJETA_CREDITO','EXITOSO','TXN-2026-0008',6),
 (9,'2026-03-01 13:05:00',170000,'EFECTIVO','EXITOSO',NULL,7),
 (10,'2026-03-10 09:05:00',360000,'TARJETA_CREDITO','FALLIDO','TXN-2026-0009',8),
 (11,'2026-03-10 09:10:00',360000,'TRANSFERENCIA','EXITOSO',NULL,8),
 (12,'2026-04-01 16:05:00',95000,'PSE','EXITOSO','PSE-2026-0010',9),
 (13,'2026-04-20 10:05:00',240000,'TARJETA_CREDITO','EXITOSO','TXN-2026-0011',10),
 (14,'2026-05-14 09:05:00',240000,'TARJETA_CREDITO','REEMBOLSADO','TXN-2026-0012',10),
 (15,'2026-09-28 12:05:00',70000,'PSE','EXITOSO','PSE-2026-0013',11),
 (16,'2026-10-25 09:05:00',240000,'TARJETA_DEBITO','EXITOSO','TXN-2026-0014',12),
 (17,'2026-10-30 19:05:00',100000,'PSE','PENDIENTE','PSE-2026-0015',13),
 (18,'2026-11-02 08:35:00',255000,'TARJETA_CREDITO','EXITOSO','TXN-2026-0016',14);
-- Reserva 4: el cliente canceló con 8 días de anticipación -> reembolso del 80 %.
-- Reserva 10: el operador canceló la salida 7 -> reembolso del 100 %.
-- Reserva 11: el cliente canceló con 2 días de anticipación -> sin reembolso.

-- -------------------------- NOTIFICACIONES --------------------------
INSERT INTO notificacion (id_notificacion, tipo, canal, mensaje, fecha_programada, fecha_envio,
                          estado, id_cliente, id_reserva) VALUES
 (1,'CONFIRMACION_RESERVA','SMS','Tu reserva #1 para Caminata Valle de Cocora quedó confirmada.','2026-01-05 10:21:00','2026-01-05 10:21:00','LEIDA',1,1),
 (2,'CAMBIO_ESTADO','CORREO','La salida del 16/05/2026 fue cancelada por el operador. Se reembolsó el 100 % de tu pago.','2026-05-14 09:10:00','2026-05-14 09:10:00','ENVIADA',2,10),
 (3,'CONFIRMACION_PAGO','WHATSAPP','No pudimos procesar tu pago con tarjeta para la reserva #8.','2026-03-10 09:06:00',NULL,'FALLIDA',6,8),
 (4,'PROMOCION','SMS','Este mes 15 % de descuento en tours cafeteros del Quindío.','2026-10-01 09:00:00','2026-10-01 09:00:00','ENVIADA',1,NULL),
 (5,'CONFIRMACION_PAGO','CORREO','Recibimos tu pago de $255.000 para la reserva #14.','2026-11-02 08:36:00','2026-11-02 08:36:00','ENVIADA',4,14),
 (6,'RECORDATORIO_RESERVA','WHATSAPP','Recuerda tu Caminata Valle de Cocora el 21/11/2026 a las 7:00 a. m.','2026-11-18 08:00:00',NULL,'PENDIENTE',5,12),
 (7,'RECORDATORIO_RESERVA','SMS','Recuerda tu Parapente en Buenavista el 28/11/2026 a las 9:00 a. m.','2026-11-27 08:00:00',NULL,'PENDIENTE',1,13),
 (8,'RECORDATORIO_RESERVA','CORREO','Reminder: Coffee farm tour on 05/12/2026 at 10:00 a.m.','2026-11-28 08:00:00',NULL,'PENDIENTE',4,14);
