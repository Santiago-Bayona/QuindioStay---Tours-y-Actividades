package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Municipio;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.MunicipioRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.MunicipioResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de municipios. El nombre y el código DANE son únicos.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MunicipioService {

    private final MunicipioRepository municipioRepository;

    @Transactional(readOnly = true)
    public List<MunicipioResponse> listar() {
        return municipioRepository.findAll(Sort.by("nombre")).stream().map(MunicipioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public MunicipioResponse obtener(Long id) {
        return MunicipioResponse.de(buscar(id));
    }

    public MunicipioResponse crear(MunicipioRequest req) {
        validarUnicos(req, null);
        Municipio municipio = new Municipio(req.nombre().trim(), req.codigoDane());
        return MunicipioResponse.de(municipioRepository.saveAndFlush(municipio));
    }

    public MunicipioResponse actualizar(Long id, MunicipioRequest req) {
        Municipio municipio = buscar(id);
        validarUnicos(req, id);
        municipio.setNombre(req.nombre().trim());
        municipio.setCodigoDane(req.codigoDane());
        return MunicipioResponse.de(municipioRepository.saveAndFlush(municipio));
    }

    /** Falla con 409 si el municipio tiene prestadores (FK RESTRICT). */
    public void eliminar(Long id) {
        Municipio municipio = buscar(id);
        municipioRepository.delete(municipio);
        municipioRepository.flush();
    }

    private Municipio buscar(Long id) {
        return municipioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el municipio", id));
    }

    private void validarUnicos(MunicipioRequest req, Long idActual) {
        municipioRepository.buscarPorNombre(req.nombre().trim())
                .filter(m -> !m.getIdMunicipio().equals(idActual))
                .ifPresent(m -> {
                    throw new ReglaNegocioException("Ya existe un municipio con ese nombre");
                });
        municipioRepository.buscarPorDane(req.codigoDane())
                .filter(m -> !m.getIdMunicipio().equals(idActual))
                .ifPresent(m -> {
                    throw new ReglaNegocioException("Ya existe un municipio con ese código DANE");
                });
    }
}
