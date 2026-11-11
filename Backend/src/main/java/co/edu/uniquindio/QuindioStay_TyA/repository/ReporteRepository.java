package co.edu.uniquindio.QuindioStay_TyA.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Consultas SQL de los 10 reportes del proyecto (3 simples, 4 intermedios y 3 avanzados).
 * <p>
 * A diferencia de los demás repositorios, esta es una clase y no hereda de
 * {@code JpaRepository}: los reportes no devuelven entidades sino filas con
 * columnas calculadas (conteos, sumas, promedios). Por eso usa SQL nativo con
 * {@link NamedParameterJdbcTemplate} y devuelve cada fila como un
 * {@code Map<columna, valor>}, listo para mostrarse en una tabla o exportarse a PDF.
 */
@Repository
public class ReporteRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ReporteRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Reporte 1 (simple): ¿qué tours ofrece la plataforma y cuáles son sus características?
     * Operaciones: SELECT, ORDER BY.
     *
     * @return nombre, descripción, edad mínima y precio por persona de cada tour
     */
    public List<Map<String, Object>> toursOfertados() {
        String sql = """
                SELECT nombre, descripcion, edad_minima, precio_persona
                FROM tour
                ORDER BY nombre
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 2 (intermedio): ¿cuáles son los tours con mayor participación en cada mes?
     * Cuenta participantes de reservas no canceladas por tour y mes de la salida.
     * Operaciones: JOIN, WHERE, GROUP BY, COUNT, ORDER BY.
     *
     * @return mes, tour y número de participantes
     */
    public List<Map<String, Object>> participacionMensualPorTour() {
        String sql = """
                SELECT DATE_FORMAT(s.fecha_salida, '%Y-%m') AS mes,
                       t.nombre                            AS tour,
                       COUNT(*)                            AS participantes
                FROM tour t
                JOIN salida_tour s  ON s.id_tour    = t.id_tour
                JOIN reserva r      ON r.id_salida  = s.id_salida
                JOIN participante p ON p.id_reserva = r.id_reserva
                WHERE r.estado <> 'CANCELADA'
                GROUP BY mes, t.id_tour, t.nombre
                ORDER BY mes, participantes DESC
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 3 (intermedio): ¿qué tours presentan el mayor número de reservas canceladas?
     * Operaciones: JOIN, WHERE, GROUP BY, COUNT, ORDER BY.
     *
     * @return tour y cantidad de reservas canceladas, de mayor a menor
     */
    public List<Map<String, Object>> toursConMasCancelaciones() {
        String sql = """
                SELECT t.nombre             AS tour,
                       COUNT(r.id_reserva)  AS reservas_canceladas
                FROM tour t
                JOIN salida_tour s ON s.id_tour   = t.id_tour
                JOIN reserva r     ON r.id_salida = s.id_salida
                WHERE r.estado = 'CANCELADA'
                GROUP BY t.id_tour, t.nombre
                ORDER BY reservas_canceladas DESC, tour
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 4 (simple): ¿qué guías están registrados en la plataforma?
     * Operaciones: SELECT, ORDER BY.
     *
     * @return nombre, documento y tarjeta profesional de cada guía
     */
    public List<Map<String, Object>> guiasRegistrados() {
        String sql = """
                SELECT nombre, documento, tarjeta_profesional
                FROM guia
                ORDER BY nombre
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 5 (intermedio): ¿qué guías tienen asignada la mayor cantidad de salidas
     * en un periodo? Solo cuenta salidas no canceladas.
     * Operaciones: JOIN, WHERE, GROUP BY, COUNT, ORDER BY DESC.
     *
     * @param desde fecha inicial del periodo (inclusive)
     * @param hasta fecha final del periodo (inclusive)
     * @return guía y número de salidas asignadas
     */
    public List<Map<String, Object>> guiasConMasSalidas(LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT g.nombre           AS guia,
                       COUNT(s.id_salida) AS salidas_asignadas
                FROM guia g
                JOIN salida_tour s ON s.id_guia = g.id_guia
                WHERE s.estado <> 'CANCELADA'
                  AND s.fecha_salida BETWEEN :desde AND :hasta
                GROUP BY g.id_guia, g.nombre
                ORDER BY salidas_asignadas DESC, guia
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource()
                .addValue("desde", desde)
                .addValue("hasta", hasta));
    }

    /**
     * Reporte 6 (avanzado): ¿qué guías tienen asignadas más salidas que el promedio?
     * El promedio se calcula con una subconsulta sobre todos los guías, incluidos
     * los que no tienen salidas (por eso el LEFT JOIN).
     * Operaciones: LEFT JOIN, GROUP BY, COUNT, HAVING, subconsulta con AVG.
     *
     * @return guía, número de salidas y promedio general
     */
    public List<Map<String, Object>> guiasSobrePromedio() {
        String sql = """
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
                ORDER BY salidas_asignadas DESC, guia
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 7 (simple): ¿qué salidas están programadas y cuál es su estado?
     * Operaciones: SELECT, ORDER BY.
     *
     * @return datos de cada salida con los ids de su tour y su guía
     */
    public List<Map<String, Object>> salidasProgramadas() {
        String sql = """
                SELECT id_salida, fecha_salida, hora_salida, cupo, estado, id_tour, id_guia
                FROM salida_tour
                ORDER BY fecha_salida, hora_salida
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 8 (avanzado): ¿qué categorías presentan mayor ocupación de sus cupos?
     * Una tabla derivada calcula el % de ocupación de cada salida no cancelada
     * (participantes de reservas no canceladas / cupo * 100); luego se promedia por categoría.
     * Operaciones: JOIN, LEFT JOIN, WHERE, GROUP BY, COUNT, AVG, tabla derivada.
     *
     * @return categoría, número de salidas y ocupación promedio en %
     */
    public List<Map<String, Object>> ocupacionPorCategoria() {
        String sql = """
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
                ORDER BY ocupacion_promedio DESC
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 9 (avanzado): ¿qué tours no han conseguido participantes en ninguna salida?
     * Solo cuenta reservas no canceladas y distingue los tours sin salidas de los
     * que tienen salidas pero ningún participante válido.
     * Operaciones: subconsultas con NOT EXISTS, CASE.
     *
     * @return tour y su situación
     */
    public List<Map<String, Object>> toursSinParticipantes() {
        String sql = """
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
                ORDER BY situacion, tour
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource());
    }

    /**
     * Reporte 10 (intermedio): ¿cuánto dinero se ha recibido mensualmente por
     * categoría de tour y método de pago? Solo pagos exitosos de reservas de tours.
     * Operaciones: JOIN, WHERE, GROUP BY, SUM.
     *
     * @param desde fecha inicial del periodo (inclusive)
     * @param hasta fecha final del periodo (inclusive)
     * @return mes, categoría, método de pago y total recibido
     */
    public List<Map<String, Object>> ingresosPorCategoriaYMetodo(LocalDate desde, LocalDate hasta) {
        String sql = """
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
                  AND DATE(pg.fecha) BETWEEN :desde AND :hasta
                GROUP BY mes, c.id_categoria, c.nombre, pg.metodo
                ORDER BY mes, categoria, metodo_pago
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource()
                .addValue("desde", desde)
                .addValue("hasta", hasta));
    }
}
