package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Municipio;
import co.edu.uniquindio.QuindioStay_TyA.domain.Plan;
import co.edu.uniquindio.QuindioStay_TyA.domain.Prestador;
import co.edu.uniquindio.QuindioStay_TyA.domain.TipoPrestador;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPlan;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPrestador;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.PrestadorRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.PrestadorResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.MunicipioRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.PlanRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.PrestadorRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.TipoPrestadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión de prestadores.
 *   Solo el administrador registra prestadores y cambia su estado.
 *   El encargado puede editar los datos de su propio prestador.
 *   Un prestador nunca se borra: se retira (conserva su historial).
 *   Al retirar un prestador se inactivan sus planes.</li>
 */

@Service
@RequiredArgsConstructor
@Transactional
public class PrestadorService {

    private final PrestadorRepository prestadorRepository;
    private final MunicipioRepository municipioRepository;
    private final TipoPrestadorRepository tipoPrestadorRepository;
    private final PlanRepository planRepository;
    private final AutorizacionService autorizacion;

    @Transactional(readOnly = true)
    public List<PrestadorResponse> listar() {
        return prestadorRepository.findAll(Sort.by("nombreComercial")).stream().map(PrestadorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PrestadorResponse> listarPorMunicipio(Long idMunicipio) {
        return prestadorRepository.listarPorMunicipio(idMunicipio).stream().map(PrestadorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PrestadorResponse> listarPorTipo(Long idTipoPrestador) {
        return prestadorRepository.listarPorTipo(idTipoPrestador).stream().map(PrestadorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PrestadorResponse obtener(Long id) {
        return PrestadorResponse.de(buscar(id));
    }

    public PrestadorResponse crear(Long idUsuarioActor, PrestadorRequest req) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        validarNitUnico(req.nitDocumento(), null);
        Prestador prestador = new Prestador();
        copiarDatos(prestador, req);
        prestador.setEstado(EstadoPrestador.ACTIVO);
        return PrestadorResponse.de(prestadorRepository.saveAndFlush(prestador));
    }

    public PrestadorResponse actualizar(Long idUsuarioActor, Long id, PrestadorRequest req) {
        autorizacion.exigirGestionPrestador(idUsuarioActor, id);
        Prestador prestador = buscar(id);
        if (prestador.getEstado() == EstadoPrestador.RETIRADO) {
            throw new ReglaNegocioException("Un prestador retirado no se puede modificar");
        }
        validarNitUnico(req.nitDocumento(), id);
        copiarDatos(prestador, req);
        return PrestadorResponse.de(prestadorRepository.saveAndFlush(prestador));
    }

    /**
     * Cambia el estado del prestador (solo administrador). Un prestador
     * retirado no vuelve a activarse; al retirarlo se inactivan sus planes.
     */
    public PrestadorResponse cambiarEstado(Long idUsuarioActor, Long id, EstadoPrestador nuevoEstado) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        Prestador prestador = buscar(id);
        switch (nuevoEstado) {
            case ACTIVO -> prestador.activar();
            case SUSPENDIDO -> prestador.suspender();
            case RETIRADO -> {
                prestador.retirar();
                for (Plan plan : planRepository.listarPorPrestador(id)) {
                    if (plan.getEstado() == EstadoPlan.ACTIVO) {
                        plan.inactivar();
                    }
                }
            }
        }
        return PrestadorResponse.de(prestadorRepository.saveAndFlush(prestador));
    }

    private Prestador buscar(Long id) {
        return prestadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el prestador", id));
    }

    private void validarNitUnico(String nit, Long idActual) {
        prestadorRepository.buscarPorNit(nit)
                .filter(p -> !p.getIdPrestador().equals(idActual))
                .ifPresent(p -> {
                    throw new ReglaNegocioException("Ya existe un prestador con ese NIT o documento");
                });
    }

    private void copiarDatos(Prestador prestador, PrestadorRequest req) {
        Municipio municipio = municipioRepository.findById(req.idMunicipio())
                .orElseThrow(() -> new RecursoNoEncontradoException("el municipio", req.idMunicipio()));
        TipoPrestador tipo = tipoPrestadorRepository.findById(req.idTipoPrestador())
                .orElseThrow(() -> new RecursoNoEncontradoException("el tipo de prestador", req.idTipoPrestador()));
        prestador.setNombreComercial(req.nombreComercial().trim());
        prestador.setNitDocumento(req.nitDocumento().trim());
        prestador.setNumeroRnt(req.numeroRnt().trim());
        prestador.setDireccion(req.direccion().trim());
        prestador.setTelefono(req.telefono());
        prestador.setCorreo(req.correo().trim());
        prestador.setMunicipio(municipio);
        prestador.setTipoPrestador(tipo);
    }
}
