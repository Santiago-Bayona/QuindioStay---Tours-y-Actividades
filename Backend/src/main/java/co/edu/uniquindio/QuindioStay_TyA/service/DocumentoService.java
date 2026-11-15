package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Cliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.Pago;
import co.edu.uniquindio.QuindioStay_TyA.domain.Participante;
import co.edu.uniquindio.QuindioStay_TyA.domain.PerfilCliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.Prestador;
import co.edu.uniquindio.QuindioStay_TyA.domain.Reserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.SalidaTour;
import co.edu.uniquindio.QuindioStay_TyA.domain.Tour;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPago;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.ClienteRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.PagoRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReservaRepository;
import co.edu.uniquindio.QuindioStay_TyA.service.pdf.Formato;
import co.edu.uniquindio.QuindioStay_TyA.service.pdf.PlantillaPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plantillas de los documentos que produce el negocio (sección 5.3 del enunciado):
 * <ul>
 *   <li>Comprobante de reserva</li>
 *   <li>Comprobante de pago (o de reembolso)</li>
 *   <li>Factura de venta</li>
 *   <li>Autorización de participación de menores de edad</li>
 *   <li>Autorización de tratamiento de datos personales</li>
 * </ul>
 * Todos se generan en PDF con la misma {@link PlantillaPdf} y con los datos reales de
 * la base. Se ejecuta en una transacción de solo lectura para poder recorrer las
 * relaciones perezosas (participantes, pagos, tour, prestador).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentoService {

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;

    // ------------------------------------------------------------------ comprobante de reserva

    /**
     * Comprobante de reserva: datos del cliente, de la salida, participantes y estado de cuenta.
     * Se puede generar en cualquier estado; si está cancelada lo indica.
     */
    public byte[] comprobanteReserva(Long idReserva) {
        Reserva reserva = buscarReserva(idReserva);
        SalidaTour salida = reserva.getSalida();
        Tour tour = salida.getTour();

        PlantillaPdf pdf = PlantillaPdf.vertical("Comprobante de reserva",
                "N.º " + Formato.consecutivo("R", reserva.getIdReserva())
                        + " · Emitido el " + Formato.fecha(LocalDate.now()));

        pdf.seccion("Datos de la reserva");
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Estado", Formato.texto(reserva.getEstado()));
        datos.put("Fecha de la reserva", Formato.fechaHora(reserva.getFechaCreacion()));
        datos.put("Canal", Formato.texto(reserva.getCanal()));
        datos.put("Plan", reserva.getPlan().getNombre() + " (" + Formato.texto(reserva.getPlan().getTipo()) + ")");
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            datos.put("Fecha de cancelación", Formato.fechaHora(reserva.getFechaCancelacion()));
        }
        pdf.datos(datos);

        pdf.seccion("Cliente");
        pdf.datos(datosCliente(reserva.getCliente()));

        pdf.seccion("Actividad");
        pdf.datos(datosSalida(salida));

        pdf.seccion("Participantes");
        pdf.tabla(new String[]{"#", "Nombre", "Documento", "Fecha de nacimiento", "Edad el día de la salida"},
                new float[]{0.6f, 4, 2.2f, 2.2f, 2.2f},
                filasParticipantes(reserva, salida.getFechaSalida()), 0, 4);

        Map<String, String> resumen = new LinkedHashMap<>();
        resumen.put("Precio por persona", Formato.dinero(tour.getPrecioPersona()));
        resumen.put("Participantes", String.valueOf(reserva.cantidadParticipantes()));
        resumen.put("Valor total", Formato.dinero(reserva.getValorTotal()));
        resumen.put("Total pagado", Formato.dinero(reserva.totalPagadoExitoso()));
        resumen.put("Saldo pendiente", Formato.dinero(reserva.saldoPendiente()));
        pdf.resumen(resumen);

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            pdf.nota("Esta reserva fue cancelada el " + Formato.fechaHora(reserva.getFechaCancelacion())
                    + ". Los reembolsos aplicados se registran como pagos en estado Reembolsado.");
        } else {
            pdf.nota("Preséntese 15 minutos antes de la hora de salida con un documento de identidad de cada "
                    + "participante. Cancelación por el cliente con más de " + Reserva.DIAS_MINIMOS_REEMBOLSO
                    + " días de anticipación: reembolso del 80 % de lo pagado; con menos, no hay reembolso. "
                    + "Si el operador cancela la salida, se reembolsa el 100 %.");
        }
        return pdf.terminar();
    }

    // ------------------------------------------------------------------ comprobante de pago

    /**
     * Comprobante de un pago exitoso, o de un reembolso. Los pagos pendientes o fallidos
     * no generan comprobante.
     *
     * @throws ReglaNegocioException si el pago no está EXITOSO ni REEMBOLSADO
     */
    public byte[] comprobantePago(Long idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", idPago));
        boolean reembolso = pago.getEstado() == EstadoPago.REEMBOLSADO;
        if (!pago.esExitoso() && !reembolso) {
            throw new ReglaNegocioException("COMPROBANTE_NO_DISPONIBLE",
                    "Solo se generan comprobantes de pagos exitosos o de reembolsos (este pago está "
                            + Formato.texto(pago.getEstado()).toLowerCase() + ")");
        }
        Reserva reserva = pago.getReserva();
        Tour tour = reserva.getSalida().getTour();

        PlantillaPdf pdf = PlantillaPdf.vertical(reembolso ? "Comprobante de reembolso" : "Comprobante de pago",
                "N.º " + Formato.consecutivo("P", pago.getIdPago())
                        + " · Emitido el " + Formato.fecha(LocalDate.now()));

        pdf.seccion(reembolso ? "Datos del reembolso" : "Datos del pago");
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Fecha", Formato.fechaHora(pago.getFecha()));
        datos.put("Estado", Formato.texto(pago.getEstado()));
        datos.put("Método", Formato.texto(pago.getMetodo()));
        datos.put("Referencia de transacción", pago.getReferenciaTransaccion());
        pdf.datos(datos);

        pdf.seccion("Reserva asociada");
        Map<String, String> datosReserva = new LinkedHashMap<>();
        datosReserva.put("Reserva", Formato.consecutivo("R", reserva.getIdReserva())
                + " (" + Formato.texto(reserva.getEstado()) + ")");
        datosReserva.put("Cliente", reserva.getCliente().nombreCompleto());
        datosReserva.put("Tour", tour.getNombre());
        datosReserva.put("Fecha del servicio", Formato.fecha(reserva.getFechaServicio()));
        pdf.datos(datosReserva);

        Map<String, String> resumen = new LinkedHashMap<>();
        resumen.put("Valor total de la reserva", Formato.dinero(reserva.getValorTotal()));
        resumen.put("Total pagado", Formato.dinero(reserva.totalPagadoExitoso()));
        if (reembolso) {
            resumen.put("Valor reembolsado", Formato.dinero(pago.getMonto()));
        } else {
            resumen.put("Saldo pendiente", Formato.dinero(reserva.saldoPendiente()));
            resumen.put("Valor de este pago", Formato.dinero(pago.getMonto()));
        }
        pdf.resumen(resumen);

        pdf.nota(reembolso
                ? "El valor reembolsado se devuelve por el mismo medio de pago. El tiempo de acreditación "
                + "depende de la entidad financiera."
                : "Conserve este comprobante como soporte de su pago. Para ver todos los pagos de la reserva, "
                + "consulte el comprobante de reserva.");
        return pdf.terminar();
    }

    // ------------------------------------------------------------------ factura

    /**
     * Factura de venta de una reserva pagada. La emite el prestador que opera el tour.
     *
     * @throws ReglaNegocioException si la reserva está cancelada o aún tiene saldo pendiente
     */
    public byte[] factura(Long idReserva) {
        Reserva reserva = buscarReserva(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReglaNegocioException("FACTURA_NO_DISPONIBLE", "No se factura una reserva cancelada");
        }
        if (!reserva.estaPagada()) {
            throw new ReglaNegocioException("FACTURA_NO_DISPONIBLE",
                    "La reserva tiene un saldo pendiente de " + Formato.dinero(reserva.saldoPendiente())
                            + ": se factura cuando esté pagada");
        }
        SalidaTour salida = reserva.getSalida();
        Tour tour = salida.getTour();
        Prestador prestador = tour.getPrestador();
        Cliente cliente = reserva.getCliente();

        PlantillaPdf pdf = PlantillaPdf.vertical("Factura de venta",
                "N.º " + Formato.consecutivo("FV", reserva.getIdReserva())
                        + " · Fecha de expedición " + Formato.fecha(LocalDate.now()));

        pdf.seccion("Emisor");
        Map<String, String> emisor = new LinkedHashMap<>();
        emisor.put("Razón social", prestador.getNombreComercial());
        emisor.put("NIT", prestador.getNitDocumento());
        emisor.put("Registro Nacional de Turismo", prestador.getNumeroRnt());
        String municipio = prestador.getMunicipio().getNombre();
        emisor.put("Dirección", prestador.getDireccion().contains(municipio)
                ? prestador.getDireccion() + " (Quindío)"
                : prestador.getDireccion() + ", " + municipio + " (Quindío)");
        emisor.put("Teléfono", prestador.getTelefono());
        emisor.put("Correo", prestador.getCorreo());
        pdf.datos(emisor);

        pdf.seccion("Adquiriente");
        pdf.datos(datosCliente(cliente));

        pdf.seccion("Detalle");
        String concepto = tour.getNombre() + " — " + reserva.getPlan().getNombre()
                + "\nSalida del " + Formato.fecha(salida.getFechaSalida()) + " a las " + Formato.hora(salida.getHoraSalida())
                + " · Reserva " + Formato.consecutivo("R", reserva.getIdReserva());
        List<String[]> items = new ArrayList<>();
        items.add(new String[]{concepto, String.valueOf(reserva.cantidadParticipantes()),
                Formato.dinero(tour.getPrecioPersona()), Formato.dinero(reserva.getValorTotal())});
        pdf.tabla(new String[]{"Concepto", "Cantidad", "Valor unitario", "Valor total"},
                new float[]{6, 1.4f, 2, 2}, items, 1, 2, 3);

        Map<String, String> resumen = new LinkedHashMap<>();
        resumen.put("Subtotal", Formato.dinero(reserva.getValorTotal()));
        resumen.put("Total a pagar", Formato.dinero(reserva.getValorTotal()));
        pdf.resumen(resumen);

        pdf.seccion("Forma de pago");
        List<String[]> pagos = reserva.getPagos().stream()
                .filter(Pago::esExitoso)
                .sorted(Comparator.comparing(Pago::getFecha))
                .map(p -> new String[]{Formato.fechaHora(p.getFecha()), Formato.texto(p.getMetodo()),
                        p.getReferenciaTransaccion(), Formato.dinero(p.getMonto())})
                .toList();
        pdf.tabla(new String[]{"Fecha", "Método", "Referencia", "Valor"},
                new float[]{2.4f, 2.6f, 3, 2}, pagos, 3);

        pdf.nota("Documento generado por la plataforma QuindíoStay con fines académicos. No reemplaza la "
                + "factura electrónica de venta que el prestador debe expedir ante la DIAN.");
        return pdf.terminar();
    }

    // ------------------------------------------------------------------ autorización de menores

    /**
     * Autorización de participación de menores de edad en la actividad, para que la firme
     * el padre, la madre o el representante legal. Lista solo los participantes que son
     * menores de 18 años el día de la salida.
     *
     * @throws ReglaNegocioException si la reserva está cancelada o no tiene menores
     */
    public byte[] autorizacionMenores(Long idReserva) {
        Reserva reserva = buscarReserva(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReglaNegocioException("AUTORIZACION_NO_DISPONIBLE", "La reserva está cancelada");
        }
        SalidaTour salida = reserva.getSalida();
        Tour tour = salida.getTour();
        Prestador prestador = tour.getPrestador();
        LocalDate fechaSalida = salida.getFechaSalida();

        List<Participante> menores = participantesOrdenados(reserva).stream()
                .filter(p -> p.esMenorDeEdad(fechaSalida))
                .toList();
        if (menores.isEmpty()) {
            throw new ReglaNegocioException("AUTORIZACION_NO_DISPONIBLE",
                    "La reserva no tiene participantes menores de edad el día de la salida");
        }
        Cliente cliente = reserva.getCliente();

        PlantillaPdf pdf = PlantillaPdf.vertical("Autorización para menores de edad",
                "Reserva " + Formato.consecutivo("R", reserva.getIdReserva())
                        + " · Emitida el " + Formato.fecha(LocalDate.now()));

        pdf.seccion("Actividad");
        pdf.datos(datosSalida(salida));

        pdf.seccion("Autorización");
        pdf.parrafo("Yo, _______________________________________, identificado(a) con documento "
                + "N.º ____________________, en calidad de padre, madre o representante legal, autorizo a los "
                + "menores de edad relacionados a continuación para participar en la actividad «" + tour.getNombre()
                + "», operada por " + prestador.getNombreComercial() + " (RNT " + prestador.getNumeroRnt()
                + "), el día " + Formato.fecha(fechaSalida) + " a las " + Formato.hora(salida.getHoraSalida())
                + ", reservada por " + cliente.nombreCompleto() + ".");

        List<String[]> filas = new ArrayList<>();
        for (Participante p : menores) {
            filas.add(new String[]{p.getNombre(), p.getDocumento(), Formato.fecha(p.getFechaNacimiento()),
                    p.calcularEdad(fechaSalida) + " años"});
        }
        pdf.tabla(new String[]{"Nombre del menor", "Documento", "Fecha de nacimiento", "Edad"},
                new float[]{4, 2.2f, 2.2f, 1.4f}, filas, 3);

        pdf.seccion("Declaraciones");
        List<String> declaraciones = new ArrayList<>();
        declaraciones.add("Conozco la descripción de la actividad, su duración aproximada de "
                + Formato.numero(tour.getDuracionHoras()) + " horas y su nivel de dificultad "
                + Formato.texto(tour.getDificultad()).toLowerCase() + ".");
        if (tour.getEdadMinima() != null && tour.getEdadMinima() > 0) {
            declaraciones.add("Sé que la edad mínima para esta actividad es de " + tour.getEdadMinima()
                    + " años y que los menores relacionados la cumplen el día de la salida.");
        }
        declaraciones.add("Los menores asistirán acompañados por un adulto responsable durante toda la actividad.");
        declaraciones.add("Declaro que los menores se encuentran en condiciones de salud aptas para la actividad "
                + "e informaré al guía cualquier condición médica, alergia o medicamento relevante.");
        declaraciones.add("Me comprometo a que los menores sigan las indicaciones del guía y las normas de "
                + "seguridad del operador.");
        pdf.lista(declaraciones);

        pdf.firmas("Firma del padre, madre o representante legal\nNombre:\nDocumento:\nTeléfono:",
                "Firma del adulto acompañante\nNombre:\nDocumento:\nTeléfono:");
        pdf.nota("Este documento debe entregarse firmado al guía antes de iniciar la actividad. "
                + "Sin él, los menores no podrán participar.");
        return pdf.terminar();
    }

    // ------------------------------------------------------------------ autorización de datos personales

    /**
     * Autorización de tratamiento de datos personales del cliente (Ley 1581 de 2012).
     * Refleja las preferencias de su perfil: canal de notificaciones y si acepta promociones.
     */
    public byte[] autorizacionDatos(Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", idCliente));
        PerfilCliente perfil = cliente.getPerfil();

        PlantillaPdf pdf = PlantillaPdf.vertical("Autorización de datos personales",
                "Cliente N.º " + cliente.getIdCliente() + " · Emitida el " + Formato.fecha(LocalDate.now()));

        pdf.seccion("Titular de los datos");
        pdf.datos(datosCliente(cliente));

        pdf.seccion("Autorización");
        pdf.parrafo("En cumplimiento de la Ley 1581 de 2012 y sus decretos reglamentarios, yo, "
                + cliente.nombreCompleto() + ", identificado(a) con " + Formato.texto(cliente.getTipoDocumento()).toLowerCase()
                + " N.º " + cliente.getNumeroDocumento() + ", autorizo de manera previa, expresa e informada a "
                + "QuindíoStay y a los prestadores turísticos con los que realice reservas, para recolectar, "
                + "almacenar, usar y actualizar mis datos personales con las siguientes finalidades:");

        List<String> finalidades = new ArrayList<>();
        finalidades.add("Gestionar mis reservas, pagos, comprobantes y facturas.");
        finalidades.add("Enviarme recordatorios y notificaciones sobre mis reservas"
                + (perfil != null ? " por " + Formato.texto(perfil.getCanalNotificacionPreferido()).toLowerCase()
                + ", con " + perfil.getAnticipacionRecordatorios() + " día(s) de anticipación" : "") + ".");
        finalidades.add("Compartir con el prestador de cada actividad los datos necesarios para prestar el servicio "
                + "(nombre, documento y datos de los participantes).");
        if (perfil != null && perfil.recibePromociones()) {
            finalidades.add("Enviarme promociones y novedades de la plataforma, que acepté recibir en mi perfil.");
        }
        finalidades.add("Generar estadísticas internas sin identificarme personalmente.");
        pdf.lista(finalidades);

        pdf.parrafo("Como titular tengo derecho a conocer, actualizar y rectificar mis datos, solicitar prueba "
                + "de esta autorización, ser informado del uso que se les da, revocar la autorización o pedir su "
                + "supresión cuando no se respeten los principios y garantías legales, y presentar quejas ante la "
                + "Superintendencia de Industria y Comercio.");
        if (perfil == null || !perfil.recibePromociones()) {
            pdf.nota("El titular no ha aceptado recibir promociones: sus datos no se usarán con fines comerciales.");
        }

        pdf.firmas("Firma del titular\nNombre: " + cliente.nombreCompleto()
                + "\nDocumento: " + cliente.getNumeroDocumento() + "\nFecha:");
        return pdf.terminar();
    }

    // ------------------------------------------------------------------ apoyo

    private Reserva buscarReserva(Long idReserva) {
        return reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", idReserva));
    }

    private static Map<String, String> datosCliente(Cliente c) {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Nombre", c.nombreCompleto());
        datos.put("Documento", Formato.texto(c.getTipoDocumento()) + " " + c.getNumeroDocumento());
        datos.put("Correo", c.getCorreo());
        datos.put("Teléfono", c.getTelefono());
        datos.put("Ciudad de origen", c.getCiudadOrigen());
        return datos;
    }

    private static Map<String, String> datosSalida(SalidaTour s) {
        Tour tour = s.getTour();
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Tour", tour.getNombre());
        datos.put("Operador", tour.getPrestador().getNombreComercial());
        datos.put("Fecha y hora", Formato.fecha(s.getFechaSalida()) + " · " + Formato.hora(s.getHoraSalida()));
        datos.put("Duración", Formato.numero(tour.getDuracionHoras()) + " horas");
        datos.put("Guía", s.getGuia().getNombre());
        datos.put("Idioma", s.getIdioma().getNombreIdioma());
        datos.put("Dificultad", Formato.texto(tour.getDificultad()));
        datos.put("Edad mínima", tour.getEdadMinima() == null || tour.getEdadMinima() == 0
                ? "Sin restricción" : tour.getEdadMinima() + " años");
        return datos;
    }

    private static List<Participante> participantesOrdenados(Reserva r) {
        return r.getParticipantes().stream()
                .sorted(Comparator.comparing(Participante::getNumParticipante))
                .toList();
    }

    private static List<String[]> filasParticipantes(Reserva r, LocalDate fecha) {
        List<String[]> filas = new ArrayList<>();
        for (Participante p : participantesOrdenados(r)) {
            filas.add(new String[]{String.valueOf(p.getNumParticipante()), p.getNombre(), p.getDocumento(),
                    Formato.fecha(p.getFechaNacimiento()), p.calcularEdad(fecha) + " años"});
        }
        return filas;
    }

    /** @return nombre sugerido del archivo, p. ej. {@code factura-FV-000012.pdf} */
    public static String nombreArchivo(String tipo, String prefijo, Long id) {
        return tipo + "-" + Formato.consecutivo(prefijo, id) + ".pdf";
    }

}
