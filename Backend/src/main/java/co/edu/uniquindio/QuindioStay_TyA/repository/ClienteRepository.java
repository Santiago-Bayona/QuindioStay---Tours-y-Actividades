package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Cliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Acceso a datos de {@link Cliente}.
 * <p>
 * El {@code PerfilCliente} no tiene repositorio propio: se guarda en cascada con
 * su cliente (relación 1 a 1 con {@code cascade = ALL}).
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por su correo de contacto.
     *
     * @param correo correo del cliente
     * @return el cliente, o vacío si no existe
     */
    @Query("SELECT c FROM Cliente c WHERE c.correo = :correo")
    Optional<Cliente> buscarPorCorreo(@Param("correo") String correo);

    /**
     * Busca un cliente por su documento. El par tipo y número identifica a una persona.
     *
     * @param tipoDocumento   tipo de documento
     * @param numeroDocumento número de documento
     * @return el cliente, o vacío si no existe
     */
    @Query("SELECT c FROM Cliente c WHERE c.tipoDocumento = :tipo AND c.numeroDocumento = :numero")
    Optional<Cliente> buscarPorDocumento(@Param("tipo") TipoDocumento tipoDocumento,
                                         @Param("numero") String numeroDocumento);

    /**
     * Busca un cliente por el correo de su cuenta, con su perfil ya cargado.
     * Es la consulta del inicio de sesión del cliente.
     *
     * @param correoCuenta correo con el que inicia sesión
     * @return el cliente con su perfil, o vacío si no existe
     */
    @Query("SELECT c FROM Cliente c JOIN FETCH c.perfil p WHERE p.correoCuenta = :correoCuenta")
    Optional<Cliente> buscarPorCorreoCuenta(@Param("correoCuenta") String correoCuenta);
}
