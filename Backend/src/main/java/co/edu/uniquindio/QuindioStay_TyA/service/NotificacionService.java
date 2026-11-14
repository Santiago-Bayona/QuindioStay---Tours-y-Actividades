package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Cliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.Notificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.PerfilCliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.Reserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.NotificacionRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.NotificacionResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.ClienteRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.NotificacionRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Notificaciones a los clientes.
 *
 *   Una promoción no se liga a una reserva y solo se envía a clientes que aceptan promociones.
 *   Un recordatorio siempre lleva reserva.
 *   El canal por defecto es el preferido del perfil del cliente (correo si no tiene perfil).
 *   El envío es simulado: {@link #enviarPendientes()} marca como enviadas las notificaciones
 *       vencidas; la integración con un proveedor real de correo/SMS/WhatsApp queda fuera del alcance.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NotificacionRepository notificacionRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar() {
        return notificacionRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaProgramada")).stream()
                .map(NotificacionResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorCliente(Long idCliente) {
        return notificacionRepository.listarPorCliente(idCliente).stream().map(NotificacionResponse::de).toList();
    }

    /** Notificaciones pendientes cuya fecha programada ya llegó. */
    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPendientes() {
        return notificacionRepository.listarPendientes(LocalDateTime.now()).stream()
                .map(NotificacionResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public NotificacionResponse obtener(Long id) {
        return NotificacionResponse.de(buscar(id));
    }

    public NotificacionResponse crear(NotificacionRequest req) {
        Cliente cliente = clienteRepository.findById(req.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("el cliente", req.idCliente()));
        Reserva reserva = null;
        if (req.idReserva() != null) {
            reserva = reservaRepository.findById(req.idReserva())
                    .orElseThrow(() -> new RecursoNoEncontradoException("la reserva", req.idReserva()));
            if (!reserva.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
                throw new ReglaNegocioException("La reserva no pertenece a ese cliente");
            }
        }
        if (req.tipo() == TipoNotificacion.PROMOCION) {
            PerfilCliente perfil = cliente.getPerfil();
            if (perfil == null || !perfil.recibePromociones()) {
                throw new ReglaNegocioException("El cliente no acepta promociones");
            }
        }
        // El constructor valida: promoción sin reserva, recordatorio con reserva.
        Notificacion notificacion = new Notificacion(req.tipo(), req.canal(), req.mensaje().trim(),
                req.fechaProgramada(), cliente, reserva);
        return NotificacionResponse.de(notificacionRepository.saveAndFlush(notificacion));
    }

    /** Cambia la fecha de una notificación que todavía no se ha enviado. */
    public NotificacionResponse reprogramar(Long id, LocalDateTime nuevaFecha) {
        Notificacion notificacion = buscar(id);
        if (notificacion.getEstado() != EstadoNotificacion.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede reprogramar una notificación pendiente");
        }
        notificacion.setFechaProgramada(nuevaFecha);
        return NotificacionResponse.de(notificacionRepository.saveAndFlush(notificacion));
    }

    public void eliminar(Long id) {
        Notificacion notificacion = buscar(id);
        if (notificacion.getEstado() != EstadoNotificacion.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede borrar una notificación pendiente");
        }
        notificacionRepository.delete(notificacion);
        notificacionRepository.flush();
    }

    public NotificacionResponse marcarLeida(Long id) {
        Notificacion notificacion = buscar(id);
        notificacion.marcarLeida();
        return NotificacionResponse.de(notificacionRepository.saveAndFlush(notificacion));
    }

    /**
     * Envío simulado: marca como enviadas todas las pendientes cuya fecha ya llegó.
     *
     * @return cuántas notificaciones se enviaron
     */
    public int enviarPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Notificacion> pendientes = notificacionRepository.listarPendientes(ahora);
        for (Notificacion notificacion : pendientes) {
            notificacion.marcarEnviada(ahora);
        }
        notificacionRepository.flush();
        return pendientes.size();
    }

    // ---------------------------------------------------------------
    // Uso interno: los llaman ReservaService y PagoService (públicos para que funcione el proxy transaccional)
    // ---------------------------------------------------------------

    /** Crea una notificación automática por el canal preferido del cliente. */
    public void notificarEvento(Cliente cliente, Reserva reserva, TipoNotificacion tipo, String mensaje,
                         LocalDateTime programada) {
        CanalNotificacion canal = cliente.getPerfil() != null
                ? cliente.getPerfil().getCanalNotificacionPreferido()
                : CanalNotificacion.CORREO;
        notificacionRepository.save(new Notificacion(tipo, canal, mensaje, programada, cliente, reserva));
    }

    /**
     * Programa el recordatorio de una reserva confirmada: N días antes del
     * servicio (según el perfil del cliente, 1 día si no tiene) a las 9:00.
     * Si esa fecha ya pasó, se programa para ahora.
     */
    public void programarRecordatorio(Reserva reserva) {
        PerfilCliente perfil = reserva.getCliente().getPerfil();
        int dias = perfil != null ? perfil.getAnticipacionRecordatorios() : 1;
        LocalDateTime fecha = reserva.getFechaServicio().minusDays(dias).atTime(9, 0);
        LocalDateTime ahora = LocalDateTime.now();
        if (fecha.isBefore(ahora)) {
            fecha = ahora;
        }
        String mensaje = "Recordatorio: tu tour " + reserva.getSalida().getTour().getNombre()
                + " es el " + reserva.getFechaServicio().format(FECHA)
                + " (reserva " + reserva.getIdReserva() + ").";
        notificarEvento(reserva.getCliente(), reserva, TipoNotificacion.RECORDATORIO_RESERVA, mensaje, fecha);
    }

    private Notificacion buscar(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la notificación", id));
    }
}
