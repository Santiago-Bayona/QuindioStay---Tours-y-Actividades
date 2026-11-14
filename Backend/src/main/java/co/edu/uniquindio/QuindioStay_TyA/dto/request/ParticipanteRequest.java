package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Persona que viaja en una reserva.
 */
public record ParticipanteRequest(
        @NotBlank(message = "El nombre del participante es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String nombre,

        @NotBlank(message = "El documento del participante es obligatorio")
        @Size(max = 30, message = "El documento admite máximo 30 caracteres")
        String documento,

        @NotNull(message = "La fecha de nacimiento del participante es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento
) {
}
