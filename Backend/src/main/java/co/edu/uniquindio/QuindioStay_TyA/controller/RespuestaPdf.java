package co.edu.uniquindio.QuindioStay_TyA.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Arma la respuesta HTTP de un PDF.
 * <p>
 * Por defecto el navegador lo abre en una pestaña ({@code inline}); con
 * {@code descargar = true} lo descarga como archivo ({@code attachment}).
 */
final class RespuestaPdf {

    private RespuestaPdf() {
    }

    static ResponseEntity<byte[]> de(byte[] contenido, String nombreArchivo, boolean descargar) {
        ContentDisposition disposicion = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(nombreArchivo)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(contenido);
    }
}
