package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Guia;
import co.edu.uniquindio.QuindioStay_TyA.domain.Idioma;
import co.edu.uniquindio.QuindioStay_TyA.domain.SalidaTour;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoSalida;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.GuiaRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.GuiaResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.GuiaRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.IdiomaRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.SalidaTourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestión de guías y de los idiomas que hablan.
 *
 *   Todo guía habla al menos un idioma.
 *   No se le puede quitar un idioma que usa en una salida programada.
 *   Un guía con salidas registradas no se borra.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class GuiaService {

    private final GuiaRepository guiaRepository;
    private final IdiomaRepository idiomaRepository;
    private final SalidaTourRepository salidaRepository;

    @Transactional(readOnly = true)
    public List<GuiaResponse> listar() {
        return guiaRepository.findAll(Sort.by("nombre")).stream().map(GuiaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<GuiaResponse> listarPorIdioma(Long idIdioma) {
        return guiaRepository.listarPorIdioma(idIdioma).stream().map(GuiaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public GuiaResponse obtener(Long id) {
        return GuiaResponse.de(buscar(id));
    }

    public GuiaResponse crear(GuiaRequest req) {
        validarDocumentoUnico(req.documento().trim(), null);
        Guia guia = new Guia(req.nombre().trim(), req.documento().trim(), req.tarjetaProfesional().trim());
        guia.getIdiomas().addAll(cargarIdiomas(req.idIdiomas()));
        return GuiaResponse.de(guiaRepository.saveAndFlush(guia));
    }

    public GuiaResponse actualizar(Long id, GuiaRequest req) {
        Guia guia = buscar(id);
        validarDocumentoUnico(req.documento().trim(), id);
        Set<Idioma> nuevos = cargarIdiomas(req.idIdiomas());
        Set<Long> idsNuevos = new HashSet<>(req.idIdiomas());

        for (SalidaTour salida : salidaRepository.listarPorGuia(id)) {
            boolean programada = salida.getEstado() == EstadoSalida.PROGRAMADA;
            if (programada && !idsNuevos.contains(salida.getIdioma().getIdIdioma())) {
                throw new ReglaNegocioException("No se puede quitar el idioma "
                        + salida.getIdioma().getNombreIdioma()
                        + " porque el guía tiene una salida programada en ese idioma");
            }
        }
        guia.setNombre(req.nombre().trim());
        guia.setDocumento(req.documento().trim());
        guia.setTarjetaProfesional(req.tarjetaProfesional().trim());
        guia.getIdiomas().clear();
        guia.getIdiomas().addAll(nuevos);
        return GuiaResponse.de(guiaRepository.saveAndFlush(guia));
    }

    public void eliminar(Long id) {
        Guia guia = buscar(id);
        if (!salidaRepository.listarPorGuia(id).isEmpty()) {
            throw new ReglaNegocioException("El guía tiene salidas registradas y no se puede borrar");
        }
        guiaRepository.delete(guia);
        guiaRepository.flush();
    }

    private Guia buscar(Long id) {
        return guiaRepository.buscarConIdiomas(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el guía", id));
    }

    private void validarDocumentoUnico(String documento, Long idActual) {
        guiaRepository.buscarPorDocumento(documento)
                .filter(g -> !g.getIdGuia().equals(idActual))
                .ifPresent(g -> {
                    throw new ReglaNegocioException("Ya existe un guía con ese documento");
                });
    }

    private Set<Idioma> cargarIdiomas(Set<Long> ids) {
        List<Idioma> encontrados = idiomaRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            Set<Long> existentes = new HashSet<>();
            encontrados.forEach(i -> existentes.add(i.getIdIdioma()));
            Long faltante = ids.stream().filter(i -> !existentes.contains(i)).findFirst().orElse(null);
            throw new RecursoNoEncontradoException("el idioma", faltante);
        }
        return new HashSet<>(encontrados);
    }
}
