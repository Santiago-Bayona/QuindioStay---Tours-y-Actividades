package co.edu.uniquindio.QuindioStay_TyA.exception;

/**
 * Se lanza cuando se busca un registro que no existe, por ejemplo una reserva
 * o un tour con un id inválido. Se convierte en una respuesta HTTP 404.
 * <p>
 * Ejemplo de uso con un repositorio:
 * <pre>{@code
 * Tour tour = tourRepository.findById(id)
 *         .orElseThrow(() -> new RecursoNoEncontradoException("Tour", id));
 * }</pre>
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje estándar: "No existe {recurso} con id {id}".
     *
     * @param recurso nombre de la entidad buscada, por ejemplo "Reserva"
     * @param id      identificador buscado
     */
    public RecursoNoEncontradoException(String recurso, Object id) {
        super("No existe " + recurso + " con id " + id);
    }

    /**
     * Crea la excepción con un mensaje libre, para búsquedas que no son por id.
     *
     * @param mensaje explicación, por ejemplo "No existe un cliente con ese documento"
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
