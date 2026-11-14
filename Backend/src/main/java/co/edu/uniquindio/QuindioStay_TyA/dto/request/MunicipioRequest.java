package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un municipio.
 *
 * @param nombre     nombre del municipio
 * @param codigoDane código DANE de 5 dígitos
 */
public record MunicipioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "El código DANE es obligatorio")
        @Pattern(regexp = "^\\d{5}$", message = "El código DANE debe tener 5 dígitos")
        String codigoDane
) {
}
