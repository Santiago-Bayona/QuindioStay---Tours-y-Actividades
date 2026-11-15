-- =====================================================================
-- QuindíoStay · Módulo Tours y Actividades
-- Proyecto Final - Bases de Datos I (02D) · Universidad del Quindío · 2026-2
--
-- 04_Verificacion_GrupoNN.sql  ·  Opcional, después de 01 y 02
-- 1) Cuenta los registros de cada tabla (sección 8.2 del documento).
-- 2) Comprueba las reglas de negocio que no se pueden expresar con un
--    CHECK porque miran otras tablas. Cada consulta devuelve la cantidad
--    de filas que violan la regla: en todas debe salir 0.
-- =====================================================================

USE tours_actividades;

-- ---------------------------------------------------------------------
-- 1. Registros por tabla
-- ---------------------------------------------------------------------
SELECT 'municipio' AS tabla, COUNT(*) AS registros FROM municipio
UNION ALL SELECT 'tipo_prestador', COUNT(*) FROM tipo_prestador
UNION ALL SELECT 'prestador', COUNT(*) FROM prestador
UNION ALL SELECT 'cliente', COUNT(*) FROM cliente
UNION ALL SELECT 'perfil_cliente', COUNT(*) FROM perfil_cliente
UNION ALL SELECT 'plan', COUNT(*) FROM plan
UNION ALL SELECT 'rol', COUNT(*) FROM rol
UNION ALL SELECT 'usuario_sistema', COUNT(*) FROM usuario_sistema
UNION ALL SELECT 'categoria_tour', COUNT(*) FROM categoria_tour
UNION ALL SELECT 'tour', COUNT(*) FROM tour
UNION ALL SELECT 'guia', COUNT(*) FROM guia
UNION ALL SELECT 'idioma', COUNT(*) FROM idioma
UNION ALL SELECT 'guia_idioma', COUNT(*) FROM guia_idioma
UNION ALL SELECT 'salida_tour', COUNT(*) FROM salida_tour
UNION ALL SELECT 'reserva', COUNT(*) FROM reserva
UNION ALL SELECT 'participante', COUNT(*) FROM participante
UNION ALL SELECT 'pago', COUNT(*) FROM pago
UNION ALL SELECT 'notificacion', COUNT(*) FROM notificacion;

-- ---------------------------------------------------------------------
-- 2. Reglas de negocio (todas deben dar 0)
-- ---------------------------------------------------------------------

-- R01. El guía de cada salida habla el idioma en que se ofrece.
SELECT 'R01 idioma del guía' AS regla, COUNT(*) AS violaciones
FROM salida_tour s
WHERE NOT EXISTS (SELECT 1 FROM guia_idioma gi
                  WHERE gi.id_guia = s.id_guia AND gi.id_idioma = s.id_idioma)

-- R02. Un guía no tiene dos salidas activas que se crucen (inicio + duración del tour).
UNION ALL
SELECT 'R02 cruce de horarios del guía', COUNT(*)
FROM salida_tour a
JOIN tour ta ON ta.id_tour = a.id_tour
JOIN salida_tour b ON b.id_guia = a.id_guia AND b.id_salida > a.id_salida
JOIN tour tb ON tb.id_tour = b.id_tour
WHERE a.estado <> 'CANCELADA' AND b.estado <> 'CANCELADA'
  AND TIMESTAMP(a.fecha_salida, a.hora_salida)
      < TIMESTAMP(b.fecha_salida, b.hora_salida) + INTERVAL ROUND(tb.duracion_horas * 60) MINUTE
  AND TIMESTAMP(b.fecha_salida, b.hora_salida)
      < TIMESTAMP(a.fecha_salida, a.hora_salida) + INTERVAL ROUND(ta.duracion_horas * 60) MINUTE

-- R03. Los participantes de reservas no canceladas no superan el cupo de la salida.
UNION ALL
SELECT 'R03 cupo de la salida', COUNT(*)
FROM (SELECT s.id_salida
      FROM salida_tour s
      JOIN reserva r ON r.id_salida = s.id_salida AND r.estado <> 'CANCELADA'
      JOIN participante p ON p.id_reserva = r.id_reserva
      GROUP BY s.id_salida, s.cupo
      HAVING COUNT(*) > s.cupo) x

-- R04. Todos los participantes cumplen la edad mínima del tour el día de la salida.
UNION ALL
SELECT 'R04 edad mínima', COUNT(*)
FROM participante p
JOIN reserva r ON r.id_reserva = p.id_reserva
JOIN salida_tour s ON s.id_salida = r.id_salida
JOIN tour t ON t.id_tour = s.id_tour
WHERE TIMESTAMPDIFF(YEAR, p.fecha_nacimiento, s.fecha_salida) < t.edad_minima

-- R05. Una persona no aparece dos veces en reservas activas de la misma salida.
UNION ALL
SELECT 'R05 participante repetido en la salida', COUNT(*)
FROM (SELECT r.id_salida, p.documento
      FROM participante p
      JOIN reserva r ON r.id_reserva = p.id_reserva
      WHERE r.estado <> 'CANCELADA'
      GROUP BY r.id_salida, p.documento
      HAVING COUNT(*) > 1) x

-- R06. El plan de la reserva es del mismo prestador que publicó el tour.
UNION ALL
SELECT 'R06 plan del mismo prestador', COUNT(*)
FROM reserva r
JOIN plan pl ON pl.id_plan = r.id_plan
JOIN salida_tour s ON s.id_salida = r.id_salida
JOIN tour t ON t.id_tour = s.id_tour
WHERE pl.id_prestador <> t.id_prestador

-- R07. El plan estaba vigente en la fecha del servicio.
UNION ALL
SELECT 'R07 plan vigente en la fecha del servicio', COUNT(*)
FROM reserva r
JOIN plan pl ON pl.id_plan = r.id_plan
WHERE r.fecha_servicio NOT BETWEEN pl.fecha_inicio AND pl.fecha_fin

-- R08. La fecha del servicio es la fecha de la salida.
UNION ALL
SELECT 'R08 fecha del servicio = fecha de la salida', COUNT(*)
FROM reserva r
JOIN salida_tour s ON s.id_salida = r.id_salida
WHERE r.fecha_servicio <> s.fecha_salida

-- R09. Valor total = precio por persona del tour x número de participantes.
UNION ALL
SELECT 'R09 valor total', COUNT(*)
FROM reserva r
JOIN salida_tour s ON s.id_salida = r.id_salida
JOIN tour t ON t.id_tour = s.id_tour
WHERE r.valor_total <> t.precio_persona *
      (SELECT COUNT(*) FROM participante p WHERE p.id_reserva = r.id_reserva)

-- R10. Los pagos exitosos de una reserva no superan su valor total.
UNION ALL
SELECT 'R10 pagos no exceden el valor', COUNT(*)
FROM reserva r
WHERE (SELECT COALESCE(SUM(monto), 0) FROM pago
       WHERE id_reserva = r.id_reserva AND estado = 'EXITOSO') > r.valor_total

-- R11. COMPLETADA = pagada; CONFIRMADA = al menos un pago exitoso; PENDIENTE = ninguno.
UNION ALL
SELECT 'R11 estado de la reserva vs pagos', COUNT(*)
FROM (SELECT r.estado, r.valor_total,
             (SELECT COALESCE(SUM(monto), 0) FROM pago
              WHERE id_reserva = r.id_reserva AND estado = 'EXITOSO') AS pagado
      FROM reserva r) x
WHERE (estado = 'COMPLETADA' AND pagado < valor_total)
   OR (estado = 'CONFIRMADA' AND pagado = 0)
   OR (estado = 'PENDIENTE'  AND pagado > 0)

-- R12. Solo hay reservas COMPLETADA en salidas REALIZADA, y las salidas canceladas
--      no tienen reservas activas.
UNION ALL
SELECT 'R12 estado de la reserva vs salida', COUNT(*)
FROM reserva r
JOIN salida_tour s ON s.id_salida = r.id_salida
WHERE (r.estado = 'COMPLETADA' AND s.estado <> 'REALIZADA')
   OR (s.estado = 'CANCELADA' AND r.estado <> 'CANCELADA')

-- R13. Reembolsos: 100 % si el operador canceló la salida; 80 % si el cliente canceló
--      con más de 5 días de anticipación; ninguno con 5 días o menos.
UNION ALL
SELECT 'R13 reembolsos', COUNT(*)
FROM (SELECT r.id_reserva, s.estado AS estado_salida,
             DATEDIFF(r.fecha_servicio, DATE(r.fecha_cancelacion)) AS dias,
             (SELECT COALESCE(SUM(monto), 0) FROM pago
              WHERE id_reserva = r.id_reserva AND estado = 'EXITOSO') AS pagado,
             (SELECT COALESCE(SUM(monto), 0) FROM pago
              WHERE id_reserva = r.id_reserva AND estado = 'REEMBOLSADO') AS devuelto
      FROM reserva r
      JOIN salida_tour s ON s.id_salida = r.id_salida
      WHERE r.estado = 'CANCELADA') x
WHERE (estado_salida = 'CANCELADA' AND devuelto <> pagado)
   OR (estado_salida <> 'CANCELADA' AND dias > 5 AND devuelto <> ROUND(pagado * 0.80))
   OR (estado_salida <> 'CANCELADA' AND dias <= 5 AND devuelto <> 0)

-- R14. Solo las reservas canceladas tienen reembolsos.
UNION ALL
SELECT 'R14 reembolso solo en canceladas', COUNT(*)
FROM pago p
JOIN reserva r ON r.id_reserva = p.id_reserva
WHERE p.estado = 'REEMBOLSADO' AND r.estado <> 'CANCELADA'

-- R15. Ningún pago es anterior a la creación de su reserva.
UNION ALL
SELECT 'R15 pago antes de la reserva', COUNT(*)
FROM pago p
JOIN reserva r ON r.id_reserva = p.id_reserva
WHERE p.fecha < r.fecha_creacion

-- R16. Las promociones van sin reserva y solo a clientes que las aceptaron.
UNION ALL
SELECT 'R16 promociones', COUNT(*)
FROM notificacion n
LEFT JOIN perfil_cliente pc ON pc.id_cliente = n.id_cliente
WHERE n.tipo = 'PROMOCION'
  AND (n.id_reserva IS NOT NULL OR pc.acepta_promociones IS NULL OR pc.acepta_promociones = FALSE)

-- R17. Los recordatorios tienen reserva, van por el canal preferido y se programan
--      con la anticipación del perfil, a las 8:00.
UNION ALL
SELECT 'R17 recordatorios según el perfil', COUNT(*)
FROM notificacion n
LEFT JOIN reserva r ON r.id_reserva = n.id_reserva
LEFT JOIN perfil_cliente pc ON pc.id_cliente = n.id_cliente
WHERE n.tipo = 'RECORDATORIO_RESERVA'
  AND (r.id_reserva IS NULL OR pc.id_cliente IS NULL
       OR n.canal <> pc.canal_notificacion_preferido
       OR n.fecha_programada <> TIMESTAMP(r.fecha_servicio - INTERVAL pc.anticipacion_recordatorios DAY, '08:00:00'))

-- R18. El encargado de prestador tiene prestador; administrador y operador no.
UNION ALL
SELECT 'R18 encargado con prestador', COUNT(*)
FROM usuario_sistema u
JOIN rol ro ON ro.id_rol = u.id_rol
WHERE (ro.nombre = 'Encargado de prestador' AND u.id_prestador IS NULL)
   OR (ro.nombre <> 'Encargado de prestador' AND u.id_prestador IS NOT NULL)

-- R19. Los prestadores suspendidos o retirados no tienen salidas programadas.
UNION ALL
SELECT 'R19 salidas de prestadores no activos', COUNT(*)
FROM salida_tour s
JOIN tour t ON t.id_tour = s.id_tour
JOIN prestador pr ON pr.id_prestador = t.id_prestador
WHERE pr.estado <> 'ACTIVO' AND s.estado = 'PROGRAMADA';
