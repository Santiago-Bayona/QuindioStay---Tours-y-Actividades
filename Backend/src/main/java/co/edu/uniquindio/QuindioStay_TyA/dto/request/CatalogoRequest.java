package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un registro de catálogo que solo tiene nombre:
 * tipo de prestador, categoría de tour o idioma.
 *
 * @param nombre nombre del registro
 */
public record CatalogoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String nombre
) {
}
