package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Datos para registrar o editar un guía. Debe hablar al menos un idioma.
 */
public record GuiaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String nombre,

        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 30, message = "El documento admite máximo 30 caracteres")
        String documento,

        @NotBlank(message = "La tarjeta profesional es obligatoria")
        @Size(max = 40, message = "La tarjeta profesional admite máximo 40 caracteres")
        String tarjetaProfesional,

        @NotEmpty(message = "El guía debe hablar al menos un idioma")
        Set<Long> idIdiomas
) {
}
