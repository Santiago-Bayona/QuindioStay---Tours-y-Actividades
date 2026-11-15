-- =====================================================================
-- QuindíoStay · Módulo Tours y Actividades
-- Proyecto Final - Bases de Datos I (02D) · Universidad del Quindío · 2026-2
--
-- 03_Reportes_GrupoNN.sql  ·  Orden de ejecución: 3 de 3
-- Las 10 consultas de los reportes definidos en la Entrega 2:
--   3 simples (R1, R4, R7), 4 intermedios (R2, R3, R5, R10)
--   y 3 avanzados con subconsultas (R6, R8, R9).
-- Son las mismas consultas que ejecuta la aplicación (ReporteRepository);
-- allí los parámetros :desde y :hasta los envía la pantalla de reportes.
-- Solo consultan: no modifican datos.
-- =====================================================================

USE tours_actividades;

-- Periodo que usan los reportes 5 y 10 (se puede cambiar antes de ejecutar)
SET @desde = '2025-01-01';
SET @hasta = '2026-12-31';

-- ---------------------------------------------------------------------
-- R1 · Tours ofertados  (Simple)
-- Pregunta:    ¿Qué tours ofrece la plataforma y cuáles son sus características?
-- Tablas:      tour
-- Operaciones: SELECT, ORDER BY
-- ---------------------------------------------------------------------
SELECT nombre, descripcion, edad_minima, precio_persona
FROM tour
ORDER BY nombre;

-- ---------------------------------------------------------------------
-- R2 · Participación mensual por tour  (Intermedio)
-- Pregunta:    ¿Cuáles son los tours con mayor participación de clientes en cada mes?
-- Tablas:      tour, salida_tour, reserva, participante
-- Operaciones: JOIN, WHERE, GROUP BY, COUNT, ORDER BY
-- ---------------------------------------------------------------------
SELECT DATE_FORMAT(s.fecha_salida, '%Y-%m') AS mes,
       t.nombre                            AS tour,
       COUNT(*)                            AS participantes
FROM tour t
JOIN salida_tour s  ON s.id_tour    = t.id_tour
JOIN reserva r      ON r.id_salida  = s.id_salida
JOIN participante p ON p.id_reserva = r.id_reserva
WHERE r.estado <> 'CANCELADA'
GROUP BY mes, t.id_tour, t.nombre
ORDER BY mes, participantes DESC;

-- ---------------------------------------------------------------------
-- R3 · Tours con más cancelaciones  (Intermedio)
-- Pregunta:    ¿Qué tours presentan el mayor número de reservas canceladas?
-- Tablas:      tour, salida_tour, reserva
-- Operaciones: JOIN, WHERE, GROUP BY, COUNT, ORDER BY
-- ---------------------------------------------------------------------
SELECT t.nombre             AS tour,
       COUNT(r.id_reserva)  AS reservas_canceladas
FROM tour t
JOIN salida_tour s ON s.id_tour   = t.id_tour
JOIN reserva r     ON r.id_salida = s.id_salida
WHERE r.estado = 'CANCELADA'
GROUP BY t.id_tour, t.nombre
ORDER BY reservas_canceladas DESC, tour;

-- ---------------------------------------------------------------------
-- R4 · Guías registrados  (Simple)
-- Pregunta:    ¿Qué guías están registrados en la plataforma?
-- Tablas:      guia
-- Operaciones: SELECT, ORDER BY
-- ---------------------------------------------------------------------
SELECT nombre, documento, tarjeta_profesional
FROM guia
ORDER BY nombre;

-- ---------------------------------------------------------------------
-- R5 · Guías con más salidas en un periodo  (Intermedio)
-- Pregunta:    ¿Qué guías tienen asignada la mayor cantidad de salidas turísticas?
-- Tablas:      guia, salida_tour
-- Operaciones: JOIN, WHERE, BETWEEN, GROUP BY, COUNT, ORDER BY DESC
-- ---------------------------------------------------------------------
SELECT g.nombre           AS guia,
       COUNT(s.id_salida) AS salidas_asignadas
FROM guia g
JOIN salida_tour s ON s.id_guia = g.id_guia
WHERE s.estado <> 'CANCELADA'
  AND s.fecha_salida BETWEEN @desde AND @hasta
GROUP BY g.id_guia, g.nombre
ORDER BY salidas_asignadas DESC, guia;

-- ---------------------------------------------------------------------
-- R6 · Guías por encima del promedio  (Avanzado)
-- Pregunta:    ¿Qué guías tienen asignadas más salidas que el promedio de los guías registrados?
-- Tablas:      guia, salida_tour
-- Operaciones: LEFT JOIN, GROUP BY, COUNT, HAVING, subconsultas con AVG
-- ---------------------------------------------------------------------
SELECT g.nombre           AS guia,
       COUNT(s.id_salida) AS salidas_asignadas,
       (SELECT ROUND(AVG(x.total), 2)
        FROM (SELECT COUNT(s2.id_salida) AS total
              FROM guia g2
              LEFT JOIN salida_tour s2
                     ON s2.id_guia = g2.id_guia AND s2.estado <> 'CANCELADA'
              GROUP BY g2.id_guia) x) AS promedio_general
FROM guia g
LEFT JOIN salida_tour s
       ON s.id_guia = g.id_guia AND s.estado <> 'CANCELADA'
GROUP BY g.id_guia, g.nombre
HAVING COUNT(s.id_salida) > (
       SELECT AVG(x.total)
       FROM (SELECT COUNT(s2.id_salida) AS total
             FROM guia g2
             LEFT JOIN salida_tour s2
                    ON s2.id_guia = g2.id_guia AND s2.estado <> 'CANCELADA'
             GROUP BY g2.id_guia) x)
ORDER BY salidas_asignadas DESC, guia;

-- ---------------------------------------------------------------------
-- R7 · Salidas programadas  (Simple)
-- Pregunta:    ¿Qué salidas turísticas están programadas y cuál es su estado?
-- Tablas:      salida_tour
-- Operaciones: SELECT, ORDER BY
-- ---------------------------------------------------------------------
SELECT id_salida, fecha_salida, hora_salida, cupo, estado, id_tour, id_guia
FROM salida_tour
ORDER BY fecha_salida, hora_salida;

-- ---------------------------------------------------------------------
-- R8 · Ocupación por categoría  (Avanzado)
-- Pregunta:    ¿Qué categorías de tours presentan mayor ocupación de sus cupos disponibles?
-- Tablas:      categoria_tour, tour, salida_tour, reserva, participante
-- Operaciones: JOIN, LEFT JOIN, WHERE, GROUP BY, COUNT, AVG, tabla derivada
-- ---------------------------------------------------------------------
SELECT c.nombre                      AS categoria,
       COUNT(o.id_salida)            AS salidas,
       ROUND(AVG(o.porcentaje), 2)   AS ocupacion_promedio
FROM categoria_tour c
JOIN tour t ON t.id_categoria = c.id_categoria
JOIN (SELECT s.id_salida,
             s.id_tour,
             COUNT(p.num_participante) * 100.0 / s.cupo AS porcentaje
      FROM salida_tour s
      LEFT JOIN reserva r
             ON r.id_salida = s.id_salida AND r.estado <> 'CANCELADA'
      LEFT JOIN participante p
             ON p.id_reserva = r.id_reserva
      WHERE s.estado <> 'CANCELADA'
      GROUP BY s.id_salida, s.id_tour, s.cupo) o
     ON o.id_tour = t.id_tour
GROUP BY c.id_categoria, c.nombre
ORDER BY ocupacion_promedio DESC;

-- ---------------------------------------------------------------------
-- R9 · Tours sin participantes  (Avanzado)
-- Pregunta:    ¿Qué tours no han conseguido participantes en ninguna de sus salidas?
-- Tablas:      tour, salida_tour, reserva, participante
-- Operaciones: subconsultas con NOT EXISTS, CASE
-- ---------------------------------------------------------------------
SELECT t.nombre AS tour,
       CASE
         WHEN NOT EXISTS (SELECT 1 FROM salida_tour s WHERE s.id_tour = t.id_tour)
           THEN 'Sin salidas programadas'
         ELSE 'Con salidas, sin participantes'
       END AS situacion
FROM tour t
WHERE NOT EXISTS (
        SELECT 1
        FROM salida_tour s
        JOIN reserva r      ON r.id_salida  = s.id_salida
        JOIN participante p ON p.id_reserva = r.id_reserva
        WHERE s.id_tour = t.id_tour
          AND r.estado <> 'CANCELADA')
ORDER BY situacion, tour;

-- ---------------------------------------------------------------------
-- R10 · Ingresos por categoría y método de pago  (Intermedio)
-- Pregunta:    ¿Cuánto dinero se ha recibido mensualmente por categoría de tour y método de pago?
-- Tablas:      pago, reserva, salida_tour, tour, categoria_tour
-- Operaciones: JOIN, WHERE, BETWEEN, GROUP BY, SUM
-- ---------------------------------------------------------------------
SELECT DATE_FORMAT(pg.fecha, '%Y-%m') AS mes,
       c.nombre                       AS categoria,
       pg.metodo                      AS metodo_pago,
       SUM(pg.monto)                  AS total_recibido
FROM pago pg
JOIN reserva r        ON r.id_reserva   = pg.id_reserva
JOIN salida_tour s    ON s.id_salida    = r.id_salida
JOIN tour t           ON t.id_tour      = s.id_tour
JOIN categoria_tour c ON c.id_categoria = t.id_categoria
WHERE pg.estado = 'EXITOSO'
  AND DATE(pg.fecha) BETWEEN @desde AND @hasta
GROUP BY mes, c.id_categoria, c.nombre, pg.metodo
ORDER BY mes, categoria, metodo_pago;
