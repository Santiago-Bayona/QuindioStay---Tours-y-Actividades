package co.edu.uniquindio.QuindioStay_TyA.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cuerpo JSON que recibe el frontend cuando algo sale mal. Todas las respuestas
 * de error de la API tienen esta misma forma, así React las maneja en un solo lugar.
 * <p>
 * Ejemplo:
 * <pre>{@code
 * {
 *   "fecha": "2026-11-11T10:30:00",
 *   "estado": 400,
 *   "error": "Bad Request",
 *   "codigo": "CUPO_INSUFICIENTE",
 *   "mensaje": "Solo quedan 2 cupos en esta salida",
 *   "ruta": "/api/reservas"
 * }
 * }</pre>
 *
 * @param fecha   momento en que ocurrió el error
 * @param estado  código HTTP (400, 401, 403, 404, 409, 500)
 * @param error   nombre del estado HTTP
 * @param codigo  código de la regla violada; {@code null} si no aplica
 * @param mensaje explicación para mostrar al usuario
 * @param ruta    endpoint que se llamó
 * @param errores errores por campo de un formulario; {@code null} si no aplica
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaError(
        LocalDateTime fecha,
        int estado,
        String error,
        String codigo,
        String mensaje,
        String ruta,
        Map<String, String> errores
) {

    /**
     * Crea una respuesta de error sin código ni errores por campo.
     *
     * @param estado  código HTTP
     * @param error   nombre del estado HTTP
     * @param mensaje explicación
     * @param ruta    endpoint que se llamó
     * @return la respuesta de error
     */
    public static RespuestaError de(int estado, String error, String mensaje, String ruta) {
        return new RespuestaError(LocalDateTime.now(), estado, error, null, mensaje, ruta, null);
    }
}
