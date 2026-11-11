package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link UsuarioSistema}.
 */
public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, Long> {

    /**
     * Busca un usuario por su nombre de usuario, con su rol ya cargado.
     * Es la consulta del inicio de sesión de los usuarios internos.
     *
     * @param nombreUsuario nombre de usuario
     * @return el usuario, o vacío si no existe
     */
    @Query("SELECT u FROM UsuarioSistema u JOIN FETCH u.rol WHERE u.nombreUsuario = :nombreUsuario")
    Optional<UsuarioSistema> buscarPorNombreUsuario(@Param("nombreUsuario") String nombreUsuario);

    /**
     * Lista los usuarios encargados de un prestador.
     *
     * @param idPrestador id del prestador
     * @return usuarios asociados al prestador
     */
    @Query("SELECT u FROM UsuarioSistema u WHERE u.prestador.idPrestador = :idPrestador")
    List<UsuarioSistema> listarPorPrestador(@Param("idPrestador") Long idPrestador);
}
