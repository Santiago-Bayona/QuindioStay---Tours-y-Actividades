package co.edu.uniquindio.QuindioStay_TyA.controller;

/**
 * Nombre del header HTTP con el que el frontend envía el usuario de la sesión.
 * <p>
 * El proyecto no usa Spring Security: al iniciar sesión, React guarda el {@code id}
 * que devuelve {@code POST /api/auth/login/usuario} y lo envía en este header en cada
 * petición que modifica datos. Los services validan con {@code AutorizacionService}
 * que ese usuario exista, esté activo y tenga el rol necesario.
 * <p>
 * Ejemplo con axios:
 * <pre>{@code
 * axios.post('/api/planes', datos, { headers: { 'X-Usuario-Id': sesion.id } })
 * }</pre>
 */
public final class Sesion {

    /** Header que lleva el id del usuario interno que hace la operación. */
    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private Sesion() {
    }
}
