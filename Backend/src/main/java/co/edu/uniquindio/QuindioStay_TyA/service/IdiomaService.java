package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Idioma;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.CatalogoRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.CatalogoResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.IdiomaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD del catálogo de idiomas que pueden hablar los guías y tener las salidas.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class IdiomaService {

    private final IdiomaRepository idiomaRepository;

    @Transactional(readOnly = true)
    public List<CatalogoResponse> listar() {
        return idiomaRepository.findAll(Sort.by("nombreIdioma")).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public CatalogoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public CatalogoResponse crear(CatalogoRequest req) {
        validarUnico(req.nombre().trim(), null);
        return aRespuesta(idiomaRepository.saveAndFlush(new Idioma(req.nombre().trim())));
    }

    public CatalogoResponse actualizar(Long id, CatalogoRequest req) {
        Idioma idioma = buscar(id);
        validarUnico(req.nombre().trim(), id);
        idioma.setNombreIdioma(req.nombre().trim());
        return aRespuesta(idiomaRepository.saveAndFlush(idioma));
    }

    /** Falla con 409 si algún guía o salida usa el idioma. */
    public void eliminar(Long id) {
        idiomaRepository.delete(buscar(id));
        idiomaRepository.flush();
    }

    private Idioma buscar(Long id) {
        return idiomaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el idioma", id));
    }

    private void validarUnico(String nombre, Long idActual) {
        idiomaRepository.buscarPorNombre(nombre)
                .filter(i -> !i.getIdIdioma().equals(idActual))
                .ifPresent(i -> {
                    throw new ReglaNegocioException("Ya existe un idioma con ese nombre");
                });
    }

    private CatalogoResponse aRespuesta(Idioma i) {
        return new CatalogoResponse(i.getIdIdioma(), i.getNombreIdioma());
    }
}
