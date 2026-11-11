package co.edu.uniquindio.QuindioStay_TyA.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Atrapa las excepciones que salen de cualquier controlador y las convierte en
 * una {@link RespuestaError} con el código HTTP adecuado. Así ningún servicio ni
 * controlador tiene que armar respuestas de error a mano, y React siempre recibe
 * el mismo formato.
 * <p>
 * Resumen de códigos:
 * <ul>
 *   <li>400: regla de negocio violada, datos inválidos o JSON mal formado</li>
 *   <li>401: inicio de sesión fallido</li>
 *   <li>403: el rol no permite la operación</li>
 *   <li>404: el recurso no existe</li>
 *   <li>409: la base de datos rechazó el cambio (dato repetido, restricción, registro en uso)</li>
 *   <li>500: error inesperado</li>
 * </ul>
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    /**
     * Mensajes claros para las restricciones de 01_schema.sql. Cuando MySQL rechaza
     * un cambio, su mensaje incluye el nombre de la restricción; aquí se traduce.
     */
    private static final Map<String, String> MENSAJES_RESTRICCIONES = Map.ofEntries(
            Map.entry("uq_municipio_nombre", "Ya existe un municipio con ese nombre"),
            Map.entry("uq_municipio_dane", "Ya existe un municipio con ese código DANE"),
            Map.entry("uq_tipo_prestador_nombre", "Ya existe un tipo de prestador con ese nombre"),
            Map.entry("uq_prestador_nit", "Ya existe un prestador con ese NIT o documento"),
            Map.entry("uq_prestador_rnt", "Ya existe un prestador con ese número de RNT"),
            Map.entry("uq_prestador_correo", "Ya existe un prestador con ese correo"),
            Map.entry("uq_cliente_documento", "Ya existe un cliente con ese tipo y número de documento"),
            Map.entry("uq_cliente_correo", "Ya existe un cliente con ese correo"),
            Map.entry("uq_perfil_correo_cuenta", "Ya existe una cuenta con ese correo"),
            Map.entry("uq_plan_prestador_nombre", "El prestador ya tiene un plan con ese nombre"),
            Map.entry("uq_rol_nombre", "Ya existe un rol con ese nombre"),
            Map.entry("uq_usuario_nombre", "Ese nombre de usuario ya está en uso"),
            Map.entry("uq_categoria_nombre", "Ya existe una categoría con ese nombre"),
            Map.entry("uq_tour_prestador_nombre", "El prestador ya publicó un tour con ese nombre"),
            Map.entry("uq_guia_documento", "Ya existe un guía con ese documento"),
            Map.entry("uq_guia_tarjeta", "Ya existe un guía con esa tarjeta profesional"),
            Map.entry("uq_idioma_nombre", "Ya existe un idioma con ese nombre"),
            Map.entry("guia_idioma.PRIMARY", "El guía ya tiene registrado ese idioma"),
            Map.entry("uq_salida_guia_horario", "El guía ya tiene una salida que empieza a esa misma hora"),
            Map.entry("uq_participante_reserva_doc", "Esa persona ya está registrada en la reserva"),
            Map.entry("uq_pago_referencia", "Ya existe un pago con esa referencia de transacción"),
            Map.entry("ck_prestador_telefono", "El teléfono debe tener 10 dígitos y empezar por 3"),
            Map.entry("ck_cliente_telefono", "El teléfono debe tener 10 dígitos y empezar por 3"),
            Map.entry("ck_prestador_correo", "El correo no tiene un formato válido"),
            Map.entry("ck_cliente_correo", "El correo no tiene un formato válido"),
            Map.entry("ck_municipio_dane", "El código DANE debe tener 5 dígitos"),
            Map.entry("ck_plan_fechas", "La fecha de inicio del plan debe ser anterior a la fecha de fin"),
            Map.entry("ck_reserva_fecha_servicio", "La fecha del servicio no puede ser anterior a la de creación"),
            Map.entry("ck_reserva_cancelacion", "El estado y la fecha de cancelación de la reserva no son coherentes"),
            Map.entry("ck_pago_referencia", "Los pagos con tarjeta o PSE requieren referencia de transacción"),
            Map.entry("ck_notificacion_envio", "La fecha de envío no puede ser anterior a la programada"),
            Map.entry("ck_perfil_anticipacion", "La anticipación de los recordatorios debe ser 1, 3 o 7 días")
    );

    // ------------------------------------------------------------------
    // Excepciones propias del proyecto
    // ------------------------------------------------------------------

    /** Regla de negocio violada → 400. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<RespuestaError> manejarReglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        RespuestaError cuerpo = new RespuestaError(LocalDateTime.now(), 400, "Bad Request",
                ex.getCodigo(), ex.getMessage(), req.getRequestURI(), null);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** Registro inexistente → 404. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /** Inicio de sesión fallido → 401. */
    @ExceptionHandler(AutenticacionException.class)
    public ResponseEntity<RespuestaError> manejarAutenticacion(AutenticacionException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    /** Rol sin permiso → 403. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<RespuestaError> manejarAccesoDenegado(AccesoDenegadoException ex, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    // ------------------------------------------------------------------
    // Excepciones de las entidades (transiciones de estado inválidas)
    // ------------------------------------------------------------------

    /**
     * Los métodos del domain lanzan {@link IllegalStateException} o
     * {@link IllegalArgumentException} cuando una operación no es válida,
     * por ejemplo completar una reserva cancelada → 400.
     */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<RespuestaError> manejarEstadoInvalido(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // ------------------------------------------------------------------
    // Errores de los datos que envía el frontend
    // ------------------------------------------------------------------

    /**
     * Falla de las validaciones del DTO ({@code @NotBlank}, {@code @Email}, {@code @Pattern}...)
     * → 400 con el error de cada campo, para mostrarlo debajo de cada input del formulario.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError campo : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(campo.getField(), campo.getDefaultMessage());
        }
        RespuestaError cuerpo = new RespuestaError(LocalDateTime.now(), 400, "Bad Request",
                "DATOS_INVALIDOS", "Hay datos inválidos en el formulario", req.getRequestURI(), errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** JSON mal formado o valor de enum inexistente (p. ej. estado "ACTIVA") → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es válido: revise el formato y los valores enviados", req);
    }

    // ------------------------------------------------------------------
    // Errores de la base de datos
    // ------------------------------------------------------------------

    /**
     * La base de datos rechazó el cambio por un UNIQUE, un CHECK o una FK → 409.
     * Si la restricción está en {@link #MENSAJES_RESTRICCIONES} se muestra su mensaje claro.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarIntegridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        String detalle = ex.getMostSpecificCause().getMessage();
        log.warn("Restricción violada en {}: {}", req.getRequestURI(), detalle);
        return responder(HttpStatus.CONFLICT, traducirRestriccion(detalle), req);
    }

    // ------------------------------------------------------------------
    // Cualquier otro error
    // ------------------------------------------------------------------

    /**
     * Error inesperado → 500. Se registra completo en la consola, pero al
     * frontend solo le llega un mensaje genérico (no se exponen detalles internos).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarGeneral(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente de nuevo más tarde", req);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /**
     * Arma la respuesta estándar sin código de regla ni errores por campo.
     */
    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje, HttpServletRequest req) {
        RespuestaError cuerpo = RespuestaError.de(estado.value(), estado.getReasonPhrase(), mensaje, req.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }

    /**
     * Busca el nombre de una restricción conocida en el mensaje de MySQL y
     * devuelve su mensaje claro. Si no la reconoce, devuelve uno genérico.
     *
     * @param detalle mensaje original de la base de datos
     * @return mensaje para el usuario
     */
    static String traducirRestriccion(String detalle) {
        if (detalle == null) {
            return "La operación viola una restricción de la base de datos";
        }
        for (Map.Entry<String, String> restriccion : MENSAJES_RESTRICCIONES.entrySet()) {
            if (detalle.contains(restriccion.getKey())) {
                return restriccion.getValue();
            }
        }
        if (detalle.contains("foreign key constraint fails")) {
            if (detalle.contains("Cannot delete or update a parent row")) {
                return "No se puede eliminar el registro porque otros datos dependen de él";
            }
            return "Uno de los registros relacionados no existe";
        }
        if (detalle.contains("Duplicate entry")) {
            return "Ya existe un registro con esos datos";
        }
        return "La operación viola una restricción de la base de datos";
    }
}
