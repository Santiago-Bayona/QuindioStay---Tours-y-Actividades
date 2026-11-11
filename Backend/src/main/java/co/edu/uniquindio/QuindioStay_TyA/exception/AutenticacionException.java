package co.edu.uniquindio.QuindioStay_TyA.exception;

/**
 * Se lanza cuando el inicio de sesión falla: usuario inexistente, contraseña
 * incorrecta o usuario bloqueado. Se convierte en una respuesta HTTP 401.
 * <p>
 * Por seguridad, el mensaje no debe decir cuál de los dos datos estuvo mal.
 */
public class AutenticacionException extends RuntimeException {

    /** Crea la excepción con el mensaje genérico de credenciales inválidas. */
    public AutenticacionException() {
        super("Usuario o contraseña incorrectos");
    }

    /**
     * Crea la excepción con un mensaje propio, por ejemplo para un usuario bloqueado.
     *
     * @param mensaje explicación para el usuario
     */
    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
