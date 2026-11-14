package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.TipoPrestador;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.CatalogoRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.CatalogoResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.TipoPrestadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD del catálogo de tipos de prestador (agencia, guianza, transporte...).
 */

@Service
@RequiredArgsConstructor
@Transactional
public class TipoPrestadorService {

    private final TipoPrestadorRepository tipoRepository;

    @Transactional(readOnly = true)
    public List<CatalogoResponse> listar() {
        return tipoRepository.findAll(Sort.by("nombreTipo")).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public CatalogoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public CatalogoResponse crear(CatalogoRequest req) {
        validarUnico(req.nombre().trim(), null);
        return aRespuesta(tipoRepository.saveAndFlush(new TipoPrestador(req.nombre().trim())));
    }

    public CatalogoResponse actualizar(Long id, CatalogoRequest req) {
        TipoPrestador tipo = buscar(id);
        validarUnico(req.nombre().trim(), id);
        tipo.setNombreTipo(req.nombre().trim());
        return aRespuesta(tipoRepository.saveAndFlush(tipo));
    }

    /** Falla con 409 si hay prestadores de ese tipo. */
    public void eliminar(Long id) {
        tipoRepository.delete(buscar(id));
        tipoRepository.flush();
    }

    private TipoPrestador buscar(Long id) {
        return tipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el tipo de prestador", id));
    }

    private void validarUnico(String nombre, Long idActual) {
        tipoRepository.buscarPorNombre(nombre)
                .filter(t -> !t.getIdTipoPrestador().equals(idActual))
                .ifPresent(t -> {
                    throw new ReglaNegocioException("Ya existe un tipo de prestador con ese nombre");
                });
    }

    private CatalogoResponse aRespuesta(TipoPrestador t) {
        return new CatalogoResponse(t.getIdTipoPrestador(), t.getNombreTipo());
    }
}
