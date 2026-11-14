package co.edu.uniquindio.QuindioStay_TyA.dto.response;

/**
 * Registro de catálogo (tipo de prestador, categoría, idioma o rol).
 *
 * @param id     identificador
 * @param nombre nombre del registro
 */
public record CatalogoResponse(Long id, String nombre) {
}
