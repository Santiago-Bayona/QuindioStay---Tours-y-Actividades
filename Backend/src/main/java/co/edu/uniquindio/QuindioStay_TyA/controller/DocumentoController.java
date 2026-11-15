package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Descarga de los documentos del negocio en PDF (plantillas de la sección 5.3).
 * <p>
 * Todos aceptan {@code ?descargar=true} para bajar el archivo; sin él, el navegador
 * lo abre en una pestaña. Si el documento no aplica (p. ej. factura de una reserva
 * con saldo pendiente) responde 400 con el motivo.
 * <p>
 * Desde React basta con abrir la URL: {@code window.open('/api/documentos/reservas/12/factura')}.
 */
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    /** {@code GET /api/documentos/reservas/{id}/comprobante}: comprobante de reserva. */
    @GetMapping("/reservas/{id}/comprobante")
    public ResponseEntity<byte[]> comprobanteReserva(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "false") boolean descargar) {
        return RespuestaPdf.de(documentoService.comprobanteReserva(id),
                DocumentoService.nombreArchivo("comprobante-reserva", "R", id), descargar);
    }

    /** {@code GET /api/documentos/reservas/{id}/factura}: factura de venta (solo reservas pagadas). */
    @GetMapping("/reservas/{id}/factura")
    public ResponseEntity<byte[]> factura(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean descargar) {
        return RespuestaPdf.de(documentoService.factura(id),
                DocumentoService.nombreArchivo("factura", "FV", id), descargar);
    }

    /**
     * {@code GET /api/documentos/reservas/{id}/autorizacion-menores}: autorización de participación
     * de los menores de la reserva (400 si no tiene menores).
     */
    @GetMapping("/reservas/{id}/autorizacion-menores")
    public ResponseEntity<byte[]> autorizacionMenores(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "false") boolean descargar) {
        return RespuestaPdf.de(documentoService.autorizacionMenores(id),
                DocumentoService.nombreArchivo("autorizacion-menores", "R", id), descargar);
    }

    /** {@code GET /api/documentos/pagos/{id}/comprobante}: comprobante de pago o de reembolso. */
    @GetMapping("/pagos/{id}/comprobante")
    public ResponseEntity<byte[]> comprobantePago(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "false") boolean descargar) {
        return RespuestaPdf.de(documentoService.comprobantePago(id),
                DocumentoService.nombreArchivo("comprobante-pago", "P", id), descargar);
    }

    /** {@code GET /api/documentos/clientes/{id}/autorizacion-datos}: autorización de tratamiento de datos. */
    @GetMapping("/clientes/{id}/autorizacion-datos")
    public ResponseEntity<byte[]> autorizacionDatos(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "false") boolean descargar) {
        return RespuestaPdf.de(documentoService.autorizacionDatos(id),
                DocumentoService.nombreArchivo("autorizacion-datos", "C", id), descargar);
    }
}
