package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.CategoriaTour;
import co.edu.uniquindio.QuindioStay_TyA.domain.Prestador;
import co.edu.uniquindio.QuindioStay_TyA.domain.Tour;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.TourRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.TourResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.CategoriaTourRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.PrestadorRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.SalidaTourRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión del catálogo de tours.
 *
 *   El tour pertenece a un prestador activo y a una categoría.</li>
 *   Si el tour ya tiene salidas no se puede cambiar su duración ni su edad mínima (afectaría cruces de horario y participantes ya inscritos), ni borrarlo.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class TourService {

    private final TourRepository tourRepository;
    private final CategoriaTourRepository categoriaRepository;
    private final PrestadorRepository prestadorRepository;
    private final SalidaTourRepository salidaRepository;
    private final AutorizacionService autorizacion;

    @Transactional(readOnly = true)
    public List<TourResponse> listar() {
        return tourRepository.findAll(Sort.by("nombre")).stream().map(TourResponse::de).toList();
    }

    /** Tours de prestadores activos (los que se pueden reservar). */
    @Transactional(readOnly = true)
    public List<TourResponse> listarDisponibles() {
        return tourRepository.listarDisponibles().stream().map(TourResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<TourResponse> listarPorCategoria(Long idCategoria) {
        return tourRepository.listarPorCategoria(idCategoria).stream().map(TourResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<TourResponse> listarPorPrestador(Long idPrestador) {
        return tourRepository.listarPorPrestador(idPrestador).stream().map(TourResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TourResponse obtener(Long id) {
        return TourResponse.de(buscar(id));
    }

    public TourResponse crear(Long idUsuarioActor, TourRequest req) {
        autorizacion.exigirGestionPrestador(idUsuarioActor, req.idPrestador());
        Prestador prestador = prestadorRepository.findById(req.idPrestador())
                .orElseThrow(() -> new RecursoNoEncontradoException("el prestador", req.idPrestador()));
        if (!prestador.estaActivo()) {
            throw new ReglaNegocioException("Solo un prestador activo puede publicar tours");
        }
        Tour tour = new Tour();
        tour.setPrestador(prestador);
        copiarDatos(tour, req);
        return TourResponse.de(tourRepository.saveAndFlush(tour));
    }

    public TourResponse actualizar(Long idUsuarioActor, Long id, TourRequest req) {
        Tour tour = buscar(id);
        autorizacion.exigirGestionPrestador(idUsuarioActor, tour.getPrestador().getIdPrestador());
        if (!tour.getPrestador().getIdPrestador().equals(req.idPrestador())) {
            throw new ReglaNegocioException("Un tour no puede cambiarse de prestador");
        }
        boolean tieneSalidas = !salidaRepository.listarPorTour(id).isEmpty();
        if (tieneSalidas) {
            boolean cambiaDuracion = tour.getDuracionHoras().compareTo(req.duracionHoras()) != 0;
            boolean cambiaEdad = !tour.getEdadMinima().equals(req.edadMinima());
            if (cambiaDuracion || cambiaEdad) {
                throw new ReglaNegocioException(
                        "El tour ya tiene salidas: no se puede cambiar su duración ni su edad mínima");
            }
        }
        copiarDatos(tour, req);
        return TourResponse.de(tourRepository.saveAndFlush(tour));
    }

    public void eliminar(Long idUsuarioActor, Long id) {
        Tour tour = buscar(id);
        autorizacion.exigirGestionPrestador(idUsuarioActor, tour.getPrestador().getIdPrestador());
        if (!salidaRepository.listarPorTour(id).isEmpty()) {
            throw new ReglaNegocioException("El tour tiene salidas registradas y no se puede borrar");
        }
        tourRepository.delete(tour);
        tourRepository.flush();
    }

    private Tour buscar(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el tour", id));
    }

    private void copiarDatos(Tour tour, TourRequest req) {
        CategoriaTour categoria = categoriaRepository.findById(req.idCategoria())
                .orElseThrow(() -> new RecursoNoEncontradoException("la categoría", req.idCategoria()));
        tour.setNombre(req.nombre().trim());
        tour.setDescripcion(req.descripcion().trim());
        tour.setDuracionHoras(req.duracionHoras());
        tour.setDificultad(req.dificultad());
        tour.setEdadMinima(req.edadMinima());
        tour.setPrecioPersona(req.precioPersona());
        tour.setCategoria(categoria);
    }
}
