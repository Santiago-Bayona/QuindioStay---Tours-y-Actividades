package co.edu.uniquindio.QuindioStay_TyA.dto.response;

/**
 * Resultado de un inicio de sesión correcto.
 *
 * @param tipo         USUARIO (interno) o CLIENTE
 * @param id           id del usuario o del cliente
 * @param nombre       nombre para mostrar
 * @param rol          rol del usuario interno; {@code null} para clientes
 * @param idPrestador  prestador del encargado; {@code null} en los demás casos
 */
public record LoginResponse(String tipo, Long id, String nombre, String rol, Long idPrestador) {

    public static final String TIPO_USUARIO = "USUARIO";
    public static final String TIPO_CLIENTE = "CLIENTE";
}
