package co.edu.uniquindio.QuindioStay_TyA.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio del proyecto:
 * cupo insuficiente, guía que no habla el idioma de la salida, participante
 * por debajo de la edad mínima, plan no vigente, etc.
 * <p>
 * El {@link ManejadorGlobalExcepciones} la convierte en una respuesta HTTP 400
 * con el mensaje, para que el frontend lo muestre tal cual al usuario.
 * <p>
 * Ejemplo de uso en un servicio:
 * <pre>{@code
 * if (cuposDisponibles < solicitados) {
 *     throw new ReglaNegocioException("CUPO_INSUFICIENTE",
 *             "Solo quedan " + cuposDisponibles + " cupos en esta salida");
 * }
 * }</pre>
 */
public class ReglaNegocioException extends RuntimeException {

    /** Código corto de la regla violada; útil para que el frontend reaccione distinto según el caso. */
    private final String codigo;

    /**
     * Crea la excepción solo con mensaje; el código queda como {@code REGLA_NEGOCIO}.
     *
     * @param mensaje explicación para el usuario
     */
    public ReglaNegocioException(String mensaje) {
        this("REGLA_NEGOCIO", mensaje);
    }

    /**
     * Crea la excepción con un código de regla y un mensaje.
     *
     * @param codigo  código de la regla, por ejemplo {@code CUPO_INSUFICIENTE}
     * @param mensaje explicación para el usuario
     */
    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    /** @return código de la regla violada */
    public String getCodigo() {
        return codigo;
    }
}
