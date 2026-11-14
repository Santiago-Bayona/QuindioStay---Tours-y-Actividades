package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Cliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.Pago;
import co.edu.uniquindio.QuindioStay_TyA.domain.Participante;
import co.edu.uniquindio.QuindioStay_TyA.domain.Plan;
import co.edu.uniquindio.QuindioStay_TyA.domain.Reserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.SalidaTour;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.MetodoPago;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.CancelacionResponse;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.ParticipanteRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.ReservaRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.ReservaResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.ClienteRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.PagoRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.PlanRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReservaRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.SalidaTourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Reservas: el corazón de las reglas de negocio.
 *   La salida debe estar programada, en el futuro y de un prestador activo.
 *   El plan debe estar vigente en la fecha de la salida y ser del mismo prestador que el tour.
 *   Cupo: participantes ya inscritos (reservas no canceladas) + nuevos = cupo de la salida.
 *   Edad mínima: cada participante debe cumplirla el día de la salida.
 *   Un mismo documento no puede repetirse en la reserva ni estar ya inscrito en la salida.
 *   valor_total = precio por persona del tour x número de participantes (lo calcula el servidor).
 *   Cancelación: el cliente recibe el 80 % de lo pagado si cancela con más de 5 días de anticipación y nada si cancela después; si cancela el operador, el 100 %. El reembolso queda como un pago REEMBOLSADO; la reserva se conserva con estado CANCELADA.
 *
 */

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final PlanRepository planRepository;
    private final SalidaTourRepository salidaRepository;
    private final PagoRepository pagoRepository;
    private final NotificacionService notificacionService;

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar() {
        return reservaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion")).stream()
                .map(ReservaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Long id) {
        return ReservaResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorCliente(Long idCliente) {
        return reservaRepository.listarPorCliente(idCliente).stream().map(ReservaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorSalida(Long idSalida) {
        return reservaRepository.listarPorSalida(idSalida).stream().map(ReservaResponse::de).toList();
    }

    /** Reservas de los tours de un prestador (vista del encargado). */
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorPrestador(Long idPrestador) {
        return reservaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion")).stream()
                .filter(r -> r.getSalida().getTour().getPrestador().getIdPrestador().equals(idPrestador))
                .map(ReservaResponse::de).toList();
    }

    // ------------------------------------------------------------------ crear

    public ReservaResponse crear(ReservaRequest req) {
        Cliente cliente = clienteRepository.findById(req.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("el cliente", req.idCliente()));
        Plan plan = planRepository.findById(req.idPlan())
                .orElseThrow(() -> new RecursoNoEncontradoException("el plan", req.idPlan()));
        SalidaTour salida = salidaRepository.findById(req.idSalida())
                .orElseThrow(() -> new RecursoNoEncontradoException("la salida", req.idSalida()));

        validarSalidaReservable(salida);
        validarPlanParaSalida(plan, salida);

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setPlan(plan);
        reserva.setSalida(salida);
        reserva.setCanal(req.canal());
        reserva.setFechaServicio(salida.getFechaSalida());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        for (ParticipanteRequest datos : req.participantes()) {
            Participante participante = construirParticipante(datos);
            validarParticipante(reserva, salida, participante);
            reserva.agregarParticipante(participante);
        }
        validarCupo(salida, reserva.cantidadParticipantes());
        reserva.actualizarValorTotal();

        return ReservaResponse.de(reservaRepository.saveAndFlush(reserva));
    }

    // ------------------------------------------------------------------ participantes

    public ReservaResponse agregarParticipante(Long idReserva, ParticipanteRequest datos) {
        Reserva reserva = buscar(idReserva);
        exigirModificable(reserva);
        SalidaTour salida = reserva.getSalida();
        validarSalidaReservable(salida);

        Participante participante = construirParticipante(datos);
        validarParticipante(reserva, salida, participante);
        validarCupo(salida, 1);
        reserva.agregarParticipante(participante);
        reserva.actualizarValorTotal();
        return ReservaResponse.de(reservaRepository.saveAndFlush(reserva));
    }

    public ReservaResponse quitarParticipante(Long idReserva, Integer numParticipante) {
        Reserva reserva = buscar(idReserva);
        exigirModificable(reserva);
        Participante participante = reserva.getParticipantes().stream()
                .filter(p -> p.getNumParticipante().equals(numParticipante))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el participante " + numParticipante + " en la reserva " + idReserva));
        if (reserva.cantidadParticipantes() == 1) {
            throw new ReglaNegocioException("La reserva debe conservar al menos un participante: cancélela");
        }
        reserva.quitarParticipante(participante);
        reserva.actualizarValorTotal();
        if (reserva.totalPagadoExitoso().compareTo(reserva.getValorTotal()) > 0) {
            throw new ReglaNegocioException(
                    "Lo pagado supera el nuevo valor de la reserva: cancele la reserva para gestionar el reembolso");
        }
        return ReservaResponse.de(reservaRepository.saveAndFlush(reserva));
    }

    // ------------------------------------------------------------------ estados

    /** Confirmación manual: exige al menos un pago exitoso. */
    public ReservaResponse confirmar(Long idReserva) {
        Reserva reserva = buscar(idReserva);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede confirmar una reserva pendiente");
        }
        if (reserva.totalPagadoExitoso().signum() <= 0) {
            throw new ReglaNegocioException("Para confirmar la reserva debe existir al menos un pago exitoso");
        }
        aplicarConfirmacion(reserva);
        return ReservaResponse.de(reservaRepository.saveAndFlush(reserva));
    }

    /**
     * Cancela la reserva y calcula el reembolso.
     *
     * @param porOperador {@code true} si cancela el operador (100 %); {@code false} si cancela el cliente
     */
    public CancelacionResponse cancelar(Long idReserva, boolean porOperador) {
        Reserva reserva = buscar(idReserva);
        BigDecimal reembolso = cancelarInterna(reserva, porOperador);
        reservaRepository.flush();
        return new CancelacionResponse(ReservaResponse.de(reserva), reembolso);
    }

    // ------------------------------------------------------------------ uso interno (pagos y salidas)

    /** Si la reserva estaba pendiente y ya tiene un pago exitoso, la confirma. */
    public void confirmarPorPago(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
            aplicarConfirmacion(reserva);
        }
    }

    /** Al cancelarse una salida, el operador cancela todas sus reservas (reembolso del 100 %). */
    public void cancelarReservasDeSalida(Long idSalida) {
        for (Reserva reserva : reservaRepository.listarActivasPorSalida(idSalida)) {
            if (reserva.getEstado() == EstadoReserva.PENDIENTE || reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                cancelarInterna(reserva, true);
            }
        }
        reservaRepository.flush();
    }

    /**
     * Al realizarse una salida, las reservas confirmadas pasan a COMPLETADA y las que
     * seguían pendientes (sin pago) se cancelan sin reembolso.
     */
    public void cerrarReservasDeSalida(Long idSalida) {
        for (Reserva reserva : reservaRepository.listarActivasPorSalida(idSalida)) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                reserva.completar();
            } else if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                reserva.cancelar(LocalDateTime.now());
            }
        }
        reservaRepository.flush();
    }

    // ------------------------------------------------------------------ privados

    private Reserva buscar(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la reserva", id));
    }

    private void aplicarConfirmacion(Reserva reserva) {
        reserva.confirmar();
        String mensaje = "Tu reserva " + reserva.getIdReserva() + " del tour "
                + reserva.getSalida().getTour().getNombre() + " para el "
                + reserva.getFechaServicio().format(FECHA) + " fue confirmada.";
        notificacionService.notificarEvento(reserva.getCliente(), reserva,
                TipoNotificacion.CONFIRMACION_RESERVA, mensaje, LocalDateTime.now());
        notificacionService.programarRecordatorio(reserva);
    }

    private BigDecimal cancelarInterna(Reserva reserva, boolean porOperador) {
        reserva.cancelar(LocalDateTime.now());   // lanza IllegalStateException si ya está cancelada o completada
        BigDecimal reembolso = porOperador
                ? reserva.calcularReembolsoOperador()
                : reserva.calcularReembolsoCliente();
        if (reembolso.signum() > 0) {
            registrarReembolso(reserva, reembolso);
        }
        String mensaje = "Tu reserva " + reserva.getIdReserva() + " fue cancelada"
                + (porOperador ? " por el operador" : "")
                + (reembolso.signum() > 0 ? ". Se te reembolsarán $" + reembolso.toPlainString() : "") + ".";
        notificacionService.notificarEvento(reserva.getCliente(), reserva,
                TipoNotificacion.CAMBIO_ESTADO, mensaje, LocalDateTime.now());
        return reembolso;
    }

    private void registrarReembolso(Reserva reserva, BigDecimal monto) {
        MetodoPago metodo = reserva.getPagos().stream()
                .filter(Pago::esExitoso)
                .map(Pago::getMetodo)
                .reduce((primero, ultimo) -> ultimo)
                .orElse(MetodoPago.TRANSFERENCIA);
        String referencia = "RF-" + reserva.getIdReserva() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Pago reembolso = Pago.crearReembolso(monto, metodo, referencia);
        reserva.agregarPago(reembolso);
        pagoRepository.save(reembolso);
    }

    private void exigirModificable(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("No se puede modificar una reserva "
                    + reserva.getEstado().name().toLowerCase());
        }
    }

    private void validarSalidaReservable(SalidaTour salida) {
        if (!salida.estaVigente()) {
            throw new ReglaNegocioException("La salida no está disponible: debe estar programada y en el futuro");
        }
        if (!salida.getTour().getPrestador().estaActivo()) {
            throw new ReglaNegocioException("El prestador de este tour no está activo");
        }
    }

    private void validarPlanParaSalida(Plan plan, SalidaTour salida) {
        if (!plan.perteneceA(salida.getTour().getPrestador())) {
            throw new ReglaNegocioException("El plan debe ser del mismo prestador que el tour");
        }
        if (!plan.estaVigente(salida.getFechaSalida())) {
            throw new ReglaNegocioException("El plan no está activo o no está vigente en la fecha de la salida");
        }
    }

    private void validarCupo(SalidaTour salida, int nuevos) {
        long ocupados = salidaRepository.contarCuposOcupados(salida.getIdSalida());
        long libres = salida.getCupo() - ocupados;
        if (nuevos > libres) {
            throw new ReglaNegocioException("CUPO_AGOTADO",
                    "No hay cupo suficiente: la salida tiene " + Math.max(libres, 0)
                            + " lugares disponibles y se piden " + nuevos);
        }
    }

    private Participante construirParticipante(ParticipanteRequest datos) {
        return new Participante(datos.nombre().trim(), datos.documento().trim(), datos.fechaNacimiento());
    }

    private void validarParticipante(Reserva reserva, SalidaTour salida, Participante participante) {
        LocalDate fechaSalida = salida.getFechaSalida();
        String documento = participante.getDocumento();
        if (participante.getFechaNacimiento().isAfter(fechaSalida)) {
            throw new ReglaNegocioException("La fecha de nacimiento de " + participante.getNombre()
                    + " es posterior a la fecha de la salida");
        }
        if (reserva.tieneParticipante(documento)) {
            throw new ReglaNegocioException("El documento " + documento + " está repetido en la reserva");
        }
        if (reservaRepository.existeParticipanteEnSalida(salida.getIdSalida(), documento)) {
            throw new ReglaNegocioException("El documento " + documento + " ya está inscrito en esta salida");
        }
        int edad = participante.calcularEdad(fechaSalida);
        if (!salida.getTour().permiteEdad(edad)) {
            throw new ReglaNegocioException(participante.getNombre() + " tiene " + edad
                    + " años y el tour exige mínimo " + salida.getTour().getEdadMinima());
        }
    }
}
