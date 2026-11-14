package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resultado de uno de los 10 reportes, listo para pintar en una tabla o
 * exportar a PDF.
 *
 * @param numero      número del reporte (1 a 10)
 * @param titulo      nombre del reporte
 * @param descripcion qué responde el reporte
 * @param columnas    nombres de las columnas, en orden
 * @param filas       filas del resultado (clave = nombre de columna)
 * @param generadoEn  momento en que se generó
 */
public record ReporteResponse(
        int numero,
        String titulo,
        String descripcion,
        List<String> columnas,
        List<Map<String, Object>> filas,
        LocalDateTime generadoEn
) {
}
