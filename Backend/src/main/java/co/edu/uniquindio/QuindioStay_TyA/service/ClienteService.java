package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Cliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.PerfilCliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoDocumento;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.ClienteRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.ClienteResponse;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.PerfilClienteRequest;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.ClienteRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestión de clientes y de su perfil (cuenta y preferencias).
 *
 *   El correo y el par (tipo de documento, número) son únicos.
 *   La fecha de nacimiento no puede ser futura.
 *   El perfil es opcional; su contraseña se guarda cifrada con BCrypt.
 *   Un cliente con reservas no se borra (conserva el historial).
 *
 */

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll(Sort.by("apellido", "nombre")).stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorDocumento(TipoDocumento tipo, String numero) {
        return clienteRepository.buscarPorDocumento(tipo, numero.trim())
                .map(ClienteResponse::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con ese documento"));
    }

    public ClienteResponse crear(ClienteRequest req) {
        validarFechaNacimiento(req.fechaNacimiento());
        validarUnicos(req, null);
        Cliente cliente = new Cliente();
        copiarDatos(cliente, req);
        if (req.perfil() != null) {
            cliente.asignarPerfil(construirPerfil(req.perfil(), null));
        }
        return ClienteResponse.de(clienteRepository.saveAndFlush(cliente));
    }

    public ClienteResponse actualizar(Long id, ClienteRequest req) {
        Cliente cliente = buscar(id);
        validarFechaNacimiento(req.fechaNacimiento());
        validarUnicos(req, id);
        copiarDatos(cliente, req);
        PerfilClienteRequest datosPerfil = req.perfil();
        if (datosPerfil != null) {
            if (cliente.tienePerfil()) {
                validarCorreoCuenta(datosPerfil.correoCuenta(), id);
                PerfilCliente perfil = cliente.getPerfil();
                aplicarPreferencias(perfil, datosPerfil);
                if (datosPerfil.contrasena() != null && !datosPerfil.contrasena().isBlank()) {
                    perfil.setContrasenaCifrada(passwordEncoder.encode(datosPerfil.contrasena()));
                }
            } else {
                cliente.asignarPerfil(construirPerfil(datosPerfil, id));
            }
        }
        return ClienteResponse.de(clienteRepository.saveAndFlush(cliente));
    }

    public void eliminar(Long id) {
        Cliente cliente = buscar(id);
        if (!reservaRepository.listarPorCliente(id).isEmpty()) {
            throw new ReglaNegocioException("El cliente tiene reservas y no se puede borrar");
        }
        clienteRepository.delete(cliente);
        clienteRepository.flush();
    }

    private Cliente buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el cliente", id));
    }

    private void validarFechaNacimiento(LocalDate fecha) {
        if (!fecha.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de nacimiento debe ser anterior a hoy");
        }
    }

    private void validarUnicos(ClienteRequest req, Long idActual) {
        clienteRepository.buscarPorCorreo(req.correo().trim())
                .filter(c -> !c.getIdCliente().equals(idActual))
                .ifPresent(c -> {
                    throw new ReglaNegocioException("Ya existe un cliente con ese correo");
                });
        clienteRepository.buscarPorDocumento(req.tipoDocumento(), req.numeroDocumento().trim())
                .filter(c -> !c.getIdCliente().equals(idActual))
                .ifPresent(c -> {
                    throw new ReglaNegocioException("Ya existe un cliente con ese tipo y número de documento");
                });
    }

    private void validarCorreoCuenta(String correoCuenta, Long idActual) {
        clienteRepository.buscarPorCorreoCuenta(correoCuenta.trim())
                .filter(c -> !c.getIdCliente().equals(idActual))
                .ifPresent(c -> {
                    throw new ReglaNegocioException("Ya existe una cuenta con ese correo");
                });
    }

    private PerfilCliente construirPerfil(PerfilClienteRequest datos, Long idClienteActual) {
        if (datos.contrasena() == null || datos.contrasena().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria al crear la cuenta");
        }
        validarCorreoCuenta(datos.correoCuenta(), idClienteActual);
        PerfilCliente perfil = new PerfilCliente();
        aplicarPreferencias(perfil, datos);
        perfil.setContrasenaCifrada(passwordEncoder.encode(datos.contrasena()));
        return perfil;
    }

    private void aplicarPreferencias(PerfilCliente perfil, PerfilClienteRequest datos) {
        perfil.setCorreoCuenta(datos.correoCuenta().trim());
        perfil.setIdiomaPreferencia(datos.idiomaPreferencia());
        perfil.setCanalNotificacionPreferido(datos.canalNotificacionPreferido());
        perfil.cambiarAnticipacion(datos.anticipacionRecordatorios());
        perfil.setAceptaPromociones(datos.aceptaPromociones());
    }

    private void copiarDatos(Cliente cliente, ClienteRequest req) {
        cliente.setNombre(req.nombre().trim());
        cliente.setApellido(req.apellido().trim());
        cliente.setTipoDocumento(req.tipoDocumento());
        cliente.setNumeroDocumento(req.numeroDocumento().trim());
        cliente.setCorreo(req.correo().trim());
        cliente.setTelefono(req.telefono());
        cliente.setFechaNacimiento(req.fechaNacimiento());
        cliente.setCiudadOrigen(req.ciudadOrigen().trim());
    }
}
