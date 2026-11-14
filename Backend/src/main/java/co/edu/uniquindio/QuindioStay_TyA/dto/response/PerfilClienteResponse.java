package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.PerfilCliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.IdiomaPreferencia;

/**
 * Perfil devuelto por la API. Nunca incluye la contraseña.
 */
public record PerfilClienteResponse(
        String correoCuenta,
        IdiomaPreferencia idiomaPreferencia,
        CanalNotificacion canalNotificacionPreferido,
        Integer anticipacionRecordatorios,
        Boolean aceptaPromociones
) {

    public static PerfilClienteResponse de(PerfilCliente p) {
        if (p == null) {
            return null;
        }
        return new PerfilClienteResponse(p.getCorreoCuenta(), p.getIdiomaPreferencia(),
                p.getCanalNotificacionPreferido(), p.getAnticipacionRecordatorios(), p.getAceptaPromociones());
    }
}
