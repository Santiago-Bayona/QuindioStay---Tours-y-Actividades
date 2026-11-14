package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Guia;
import co.edu.uniquindio.QuindioStay_TyA.domain.Idioma;
import co.edu.uniquindio.QuindioStay_TyA.domain.SalidaTour;
import co.edu.uniquindio.QuindioStay_TyA.domain.Tour;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoSalida;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.SalidaTourRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.SalidaTourResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.GuiaRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.IdiomaRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReservaRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.SalidaTourRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Programación de salidas de los tours.
 *
 *   No se programa una salida en el pasado ni de un prestador inactivo.
 *   El guía debe hablar el idioma de la salida.
 *   El guía no puede tener dos salidas que se crucen en el tiempo (inicio + duración del tour).
 *   El cupo no puede bajar de los lugares ya ocupados.
 *   Una salida con reservas solo cambia de cupo o de guía; para lo demás se cancela y se crea otra.
 *   Cancelar una salida cancela sus reservas con reembolso del 100 %.
 *   Realizar una salida completa las reservas confirmadas.
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SalidaTourService {

    private final SalidaTourRepository salidaRepository;
    private final TourRepository tourRepository;
    private final GuiaRepository guiaRepository;
    private final IdiomaRepository idiomaRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;
    private final AutorizacionService autorizacion;

    @Transactional(readOnly = true)
    public List<SalidaTourResponse> listar() {
        return salidaRepository.findAll().stream()
                .sorted(Comparator.comparing(SalidaTour::getFechaSalida).thenComparing(SalidaTour::getHoraSalida))
                .map(this::aRespuesta).toList();
    }

    /** Salidas futuras, programadas y con cupo libre: las que se pueden reservar. */
    @Transactional(readOnly = true)
    public List<SalidaTourResponse> listarDisponibles() {
        return salidaRepository.findAll().stream()
                .filter(SalidaTour::estaVigente)
                .sorted(Comparator.comparing(SalidaTour::getFechaSalida).thenComparing(SalidaTour::getHoraSalida))
                .map(this::aRespuesta)
                .filter(s -> s.cuposDisponibles() > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalidaTourResponse> listarPorTour(Long idTour) {
        return salidaRepository.listarPorTour(idTour).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<SalidaTourResponse> listarPorGuia(Long idGuia) {
        return salidaRepository.listarPorGuia(idGuia).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public SalidaTourResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public SalidaTourResponse crear(Long idUsuarioActor, SalidaTourRequest req) {
        SalidaTour salida = new SalidaTour();
        salida.setEstado(EstadoSalida.PROGRAMADA);
        validarYAplicar(idUsuarioActor, salida, req, null);
        return aRespuesta(salidaRepository.saveAndFlush(salida));
    }

    public SalidaTourResponse actualizar(Long idUsuarioActor, Long id, SalidaTourRequest req) {
        SalidaTour salida = buscar(id);
        autorizacion.exigirOperacionSalida(idUsuarioActor, salida.getTour().getPrestador().getIdPrestador());
        if (salida.getEstado() != EstadoSalida.PROGRAMADA) {
            throw new ReglaNegocioException("Solo se puede modificar una salida programada");
        }
        long ocupados = salidaRepository.contarCuposOcupados(id);
        if (req.cupo() < ocupados) {
            throw new ReglaNegocioException("El cupo no puede ser menor que los lugares ya ocupados (" + ocupados + ")");
        }
        if (reservaRepository.existePorSalida(id)) {
            boolean cambiaAlgoFijo = !salida.getTour().getIdTour().equals(req.idTour())
                    || !salida.getFechaSalida().equals(req.fechaSalida())
                    || !salida.getHoraSalida().equals(req.horaSalida())
                    || !salida.getIdioma().getIdIdioma().equals(req.idIdioma());
            if (cambiaAlgoFijo) {
                throw new ReglaNegocioException("La salida ya tiene reservas: solo se puede cambiar el cupo o el guía. "
                        + "Para otros cambios cancele la salida y cree una nueva");
            }
        }
        validarYAplicar(idUsuarioActor, salida, req, id);
        return aRespuesta(salidaRepository.saveAndFlush(salida));
    }

    /** Cancela la salida y todas sus reservas (con reembolso del 100 %). */
    public SalidaTourResponse cancelar(Long idUsuarioActor, Long id) {
        SalidaTour salida = buscar(id);
        autorizacion.exigirOperacionSalida(idUsuarioActor, salida.getTour().getPrestador().getIdPrestador());
        if (salida.estaCancelada()) {
            throw new ReglaNegocioException("La salida ya está cancelada");
        }
        salida.cancelar();
        reservaService.cancelarReservasDeSalida(id);
        return aRespuesta(salidaRepository.saveAndFlush(salida));
    }

    /** Marca la salida como realizada y cierra sus reservas. Solo si ya llegó su fecha. */
    public SalidaTourResponse marcarRealizada(Long idUsuarioActor, Long id) {
        SalidaTour salida = buscar(id);
        autorizacion.exigirOperacionSalida(idUsuarioActor, salida.getTour().getPrestador().getIdPrestador());
        if (salida.getFechaSalida().isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("La salida aún no se ha realizado: es el " + salida.getFechaSalida());
        }
        salida.marcarRealizada();
        reservaService.cerrarReservasDeSalida(id);
        return aRespuesta(salidaRepository.saveAndFlush(salida));
    }

    /** Solo se borra una salida sin reservas; si no, se cancela. */
    public void eliminar(Long idUsuarioActor, Long id) {
        SalidaTour salida = buscar(id);
        autorizacion.exigirOperacionSalida(idUsuarioActor, salida.getTour().getPrestador().getIdPrestador());
        if (reservaRepository.existePorSalida(id)) {
            throw new ReglaNegocioException("La salida tiene reservas y no se puede borrar: cancélela");
        }
        salidaRepository.delete(salida);
        salidaRepository.flush();
    }

    private SalidaTour buscar(Long id) {
        return salidaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la salida", id));
    }

    private SalidaTourResponse aRespuesta(SalidaTour salida) {
        return SalidaTourResponse.de(salida, salidaRepository.contarCuposOcupados(salida.getIdSalida()));
    }

    /** Valida las reglas de negocio y copia los datos a la salida. */
    private void validarYAplicar(Long idUsuarioActor, SalidaTour salida, SalidaTourRequest req, Long idExcluir) {
        Tour tour = tourRepository.findById(req.idTour())
                .orElseThrow(() -> new RecursoNoEncontradoException("el tour", req.idTour()));
        autorizacion.exigirOperacionSalida(idUsuarioActor, tour.getPrestador().getIdPrestador());
        Guia guia = guiaRepository.findById(req.idGuia())
                .orElseThrow(() -> new RecursoNoEncontradoException("el guía", req.idGuia()));
        Idioma idioma = idiomaRepository.findById(req.idIdioma())
                .orElseThrow(() -> new RecursoNoEncontradoException("el idioma", req.idIdioma()));

        if (!tour.getPrestador().estaActivo()) {
            throw new ReglaNegocioException("El prestador del tour no está activo");
        }
        LocalDateTime inicio = LocalDateTime.of(req.fechaSalida(), req.horaSalida());
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se puede programar una salida en el pasado");
        }
        if (!guia.hablaIdioma(idioma)) {
            throw new ReglaNegocioException("El guía " + guia.getNombre() + " no habla el idioma "
                    + idioma.getNombreIdioma());
        }
        LocalDateTime fin = inicio.plusMinutes(tour.duracionEnMinutos());
        if (salidaRepository.guiaTieneCruce(guia.getIdGuia(), inicio, fin, idExcluir)) {
            throw new ReglaNegocioException("El guía " + guia.getNombre()
                    + " ya tiene otra salida que se cruza con ese horario");
        }

        salida.setTour(tour);
        salida.setGuia(guia);
        salida.setIdioma(idioma);
        salida.setFechaSalida(req.fechaSalida());
        salida.setHoraSalida(req.horaSalida());
        salida.setCupo(req.cupo());
    }
}
