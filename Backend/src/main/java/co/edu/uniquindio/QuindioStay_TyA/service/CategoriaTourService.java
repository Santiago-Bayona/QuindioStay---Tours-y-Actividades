package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.CategoriaTour;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.CatalogoRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.CatalogoResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.CategoriaTourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD del catálogo de categorías de tour (cultural, aventura, naturaleza, cafetero).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaTourService {

    private final CategoriaTourRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CatalogoResponse> listar() {
        return categoriaRepository.findAll(Sort.by("nombre")).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public CatalogoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public CatalogoResponse crear(CatalogoRequest req) {
        validarUnica(req.nombre().trim(), null);
        return aRespuesta(categoriaRepository.saveAndFlush(new CategoriaTour(req.nombre().trim())));
    }

    public CatalogoResponse actualizar(Long id, CatalogoRequest req) {
        CategoriaTour categoria = buscar(id);
        validarUnica(req.nombre().trim(), id);
        categoria.setNombre(req.nombre().trim());
        return aRespuesta(categoriaRepository.saveAndFlush(categoria));
    }

    /** Falla con 409 si hay tours en esa categoría. */
    public void eliminar(Long id) {
        categoriaRepository.delete(buscar(id));
        categoriaRepository.flush();
    }

    private CategoriaTour buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la categoría", id));
    }

    private void validarUnica(String nombre, Long idActual) {
        categoriaRepository.buscarPorNombre(nombre)
                .filter(c -> !c.getIdCategoria().equals(idActual))
                .ifPresent(c -> {
                    throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
                });
    }

    private CatalogoResponse aRespuesta(CategoriaTour c) {
        return new CatalogoResponse(c.getIdCategoria(), c.getNombre());
    }
}
