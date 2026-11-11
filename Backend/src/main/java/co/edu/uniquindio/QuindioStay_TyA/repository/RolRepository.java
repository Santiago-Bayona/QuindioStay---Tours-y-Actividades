package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Acceso a datos de {@link Rol}. Catálogo: CRUD heredado más búsqueda por nombre.
 */
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre, por ejemplo {@link Rol#ADMINISTRADOR}.
     *
     * @param nombre nombre del rol
     * @return el rol, o vacío si no existe
     */
    @Query("SELECT r FROM Rol r WHERE r.nombre = :nombre")
    Optional<Rol> buscarPorNombre(@Param("nombre") String nombre);
}
