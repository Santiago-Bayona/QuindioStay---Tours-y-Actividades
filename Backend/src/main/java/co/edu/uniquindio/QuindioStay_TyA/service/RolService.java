package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.dto.response.CatalogoResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Consulta de roles. Los tres roles (administrador, encargado de prestador y
 * operador) son fijos y vienen en los datos base, por eso solo se consultan.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService {

    private final RolRepository rolRepository;

    public List<CatalogoResponse> listar() {
        return rolRepository.findAll(Sort.by("idRol")).stream()
                .map(r -> new CatalogoResponse(r.getIdRol(), r.getNombre()))
                .toList();
    }

    public CatalogoResponse obtener(Long id) {
        return rolRepository.findById(id)
                .map(r -> new CatalogoResponse(r.getIdRol(), r.getNombre()))
                .orElseThrow(() -> new RecursoNoEncontradoException("el rol", id));
    }
}
