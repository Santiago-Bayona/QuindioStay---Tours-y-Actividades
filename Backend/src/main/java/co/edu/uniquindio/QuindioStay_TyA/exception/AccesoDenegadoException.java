package co.edu.uniquindio.QuindioStay_TyA.exception;

/**
 * Se lanza cuando un usuario autenticado intenta algo que su rol no le permite,
 * por ejemplo un encargado que quiere modificar la información de otro prestador.
 * Se convierte en una respuesta HTTP 403.
 */
public class AccesoDenegadoException extends RuntimeException {

    /** Crea la excepción con el mensaje genérico de permisos insuficientes. */
    public AccesoDenegadoException() {
        super("No tiene permisos para realizar esta operación");
    }

    /**
     * Crea la excepción con un mensaje propio.
     *
     * @param mensaje explicación para el usuario
     */
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
