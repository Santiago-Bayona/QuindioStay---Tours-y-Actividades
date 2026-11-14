package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Plan;
import co.edu.uniquindio.QuindioStay_TyA.domain.Prestador;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPlan;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPrestador;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.PlanRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.PlanResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.PlanRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.PrestadorRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestión de planes de los prestadores.
 *   El plan pertenece a un prestador activo y la vigencia debe ser coherente (inicio &lt; fin).
 *   Un plan que ya tiene reservas no se borra: se inactiva. Cambiar su precio no afecta
 *   a las reservas existentes porque estas guardan su propio valor_total.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;
    private final PrestadorRepository prestadorRepository;
    private final ReservaRepository reservaRepository;
    private final AutorizacionService autorizacion;

    @Transactional(readOnly = true)
    public List<PlanResponse> listar() {
        return planRepository.findAll(Sort.by("nombre")).stream().map(PlanResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listarPorPrestador(Long idPrestador) {
        return planRepository.listarPorPrestador(idPrestador).stream().map(PlanResponse::de).toList();
    }

    /** Planes activos y vigentes en la fecha (hoy si no se envía). */
    @Transactional(readOnly = true)
    public List<PlanResponse> listarVigentes(LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        return planRepository.listarVigentes(dia).stream().map(PlanResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listarVigentesPorPrestador(Long idPrestador, LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        return planRepository.listarVigentesPorPrestador(idPrestador, dia).stream().map(PlanResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse obtener(Long id) {
        return PlanResponse.de(buscar(id));
    }

    public PlanResponse crear(Long idUsuarioActor, PlanRequest req) {
        autorizacion.exigirGestionPrestador(idUsuarioActor, req.idPrestador());
        validarFechas(req);
        Prestador prestador = prestadorRepository.findById(req.idPrestador())
                .orElseThrow(() -> new RecursoNoEncontradoException("el prestador", req.idPrestador()));
        if (!prestador.estaActivo()) {
            throw new ReglaNegocioException("Solo un prestador activo puede crear planes");
        }
        Plan plan = new Plan();
        plan.setPrestador(prestador);
        plan.setEstado(EstadoPlan.ACTIVO);
        copiarDatos(plan, req);
        return PlanResponse.de(planRepository.saveAndFlush(plan));
    }

    public PlanResponse actualizar(Long idUsuarioActor, Long id, PlanRequest req) {
        Plan plan = buscar(id);
        autorizacion.exigirGestionPrestador(idUsuarioActor, plan.getPrestador().getIdPrestador());
        if (!plan.getPrestador().getIdPrestador().equals(req.idPrestador())) {
            throw new ReglaNegocioException("Un plan no puede cambiarse de prestador");
        }
        validarFechas(req);
        copiarDatos(plan, req);
        return PlanResponse.de(planRepository.saveAndFlush(plan));
    }

    /** Activa o inactiva el plan. Para activarlo el prestador debe estar activo. */
    public PlanResponse cambiarEstado(Long idUsuarioActor, Long id, EstadoPlan nuevoEstado) {
        Plan plan = buscar(id);
        autorizacion.exigirGestionPrestador(idUsuarioActor, plan.getPrestador().getIdPrestador());
        if (nuevoEstado == EstadoPlan.ACTIVO) {
            if (plan.getPrestador().getEstado() != EstadoPrestador.ACTIVO) {
                throw new ReglaNegocioException("No se puede activar un plan de un prestador que no está activo");
            }
            plan.activar();
        } else {
            plan.inactivar();
        }
        return PlanResponse.de(planRepository.saveAndFlush(plan));
    }

    /** Solo se borra un plan que nunca se reservó; si no, se inactiva. */
    public void eliminar(Long idUsuarioActor, Long id) {
        Plan plan = buscar(id);
        autorizacion.exigirGestionPrestador(idUsuarioActor, plan.getPrestador().getIdPrestador());
        if (reservaRepository.existePorPlan(id)) {
            throw new ReglaNegocioException("El plan ya tiene reservas y no se puede borrar: inactívelo");
        }
        planRepository.delete(plan);
        planRepository.flush();
    }

    private Plan buscar(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el plan", id));
    }

    private void validarFechas(PlanRequest req) {
        if (!req.fechaInicio().isBefore(req.fechaFin())) {
            throw new ReglaNegocioException("La fecha de inicio del plan debe ser anterior a la fecha de fin");
        }
    }

    private void copiarDatos(Plan plan, PlanRequest req) {
        plan.setNombre(req.nombre().trim());
        plan.setDescripcion(req.descripcion().trim());
        plan.setTipo(req.tipo());
        plan.setPrecio(req.precio());
        plan.setFechaInicio(req.fechaInicio());
        plan.setFechaFin(req.fechaFin());
    }
}
