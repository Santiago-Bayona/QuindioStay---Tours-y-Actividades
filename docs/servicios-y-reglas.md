# Capa service y DTOs: dónde vive cada regla

Paquetes nuevos en `Backend/src/main/java/co/edu/uniquindio/QuindioStay_TyA/`:
`dto/request` y `dto/response` (34 records con validaciones Bean Validation), `service/` (18 servicios) y `config/SeguridadConfig` (BCrypt).

Convenciones
- Los servicios devuelven DTOs de respuesta, nunca entidades (no se filtra la contraseña ni hay problemas de lazy loading).
- Los errores de regla se lanzan como `ReglaNegocioException` (400), `RecursoNoEncontradoException` (404),
  `AccesoDenegadoException` (403) o `AutenticacionException` (401); `ManejadorGlobalExcepciones` los traduce.
- Las operaciones de escritura sobre datos de un prestador, las salidas, los prestadores y los usuarios reciben
  `idUsuarioActor` (el id del usuario de la sesión). `AutorizacionService` valida su rol.
- Se usa `saveAndFlush`/`flush` para que las restricciones de la BD (UNIQUE, FK) fallen dentro del método y el
  manejador global las convierta en 409 con mensaje claro.

| Regla de negocio | Dónde se valida | Cómo demostrarla |
|---|---|---|
| Cupo de la salida | `ReservaService.validarCupo` (usa `contarCuposOcupados`) | Reservar más personas que el cupo → `CUPO_AGOTADO` |
| Idioma del guía | `SalidaTourService.validarYAplicar` (`guia.hablaIdioma`) | Crear salida con idioma que el guía no habla |
| Cruce de horarios del guía | `SalidaTourService.validarYAplicar` (`guiaTieneCruce`: inicio + duración del tour) | Dos salidas del mismo guía que se solapan |
| Edad mínima | `ReservaService.validarParticipante` | Participante menor que `edad_minima` del tour |
| Plan del mismo prestador que el tour | `ReservaService.validarPlanParaSalida` | Reservar con un plan de otro prestador |
| Plan vigente y activo en la fecha | `ReservaService.validarPlanParaSalida` | Plan inactivo o vencido |
| Salida futura y programada | `ReservaService.validarSalidaReservable` | Reservar una salida pasada o cancelada |
| Documento no repetido | `ReservaService.validarParticipante` | Mismo documento dos veces o ya inscrito en la salida |
| Valor total = precio x participantes | `Reserva.actualizarValorTotal` (llamado en `crear`) | El cliente no envía el valor |
| Reembolso 80 % (> 5 días) / 100 % operador | `ReservaService.cancelarInterna` + `Reserva.calcularReembolso*` | Cancelar con distinta anticipación |
| Primer pago exitoso confirma la reserva | `PagoService.alAprobarse` → `ReservaService.confirmarPorPago` | Registrar un pago a una reserva pendiente |
| Pago no excede el saldo | `PagoService.validarNoExcedeSaldo` | Pagar de más |
| Tarjeta/PSE exigen referencia única | `Pago.validarReferencia` + `PagoService.registrar` | Pago PSE sin referencia |
| Cancelar salida cancela sus reservas | `SalidaTourService.cancelar` → `ReservaService.cancelarReservasDeSalida` | Cancelar una salida con reservas |
| Plan/tour/guía/salida con historial no se borran | `PlanService.eliminar`, `TourService.eliminar`, `GuiaService.eliminar`, `SalidaTourService.eliminar` | Intentar borrarlos |
| Prestador no se borra, se retira (inactiva sus planes) | `PrestadorService.cambiarEstado` | Retirar un prestador |
| Promoción solo a quien la acepta y sin reserva | `NotificacionService.crear` + `Notificacion.validarReserva` | Promoción a cliente que no acepta |
| Contraseñas cifradas (BCrypt) | `ClienteService`, `UsuarioSistemaService`, `AuthService` | Login con `Clave123*` de los datos de prueba |
| Encargado solo gestiona su prestador | `AutorizacionService.exigirGestionPrestador` | Encargado editando un plan de otro prestador → 403 |
| Cliente con reservas no se borra | `ClienteService.eliminar` | Borrar un cliente con reservas |

Los 10 reportes salen de `ReporteService.ejecutar(numero, desde, hasta)`; cada uno devuelve título, columnas y filas
(`ReporteResponse`), listo para pintar en una tabla o exportar a PDF.

Limitaciones conocidas
- El cupo se valida contando participantes antes de guardar; con dos reservas simultáneas a la última plaza podría
  pasar una de más. Si se necesita, bloquear la salida con una consulta `PESSIMISTIC_WRITE`.
- El envío de notificaciones es simulado (`NotificacionService.enviarPendientes`): no hay proveedor real.
- `ReporteService.catalogo()` ejecuta los 10 reportes para sacar sus títulos; usarlo solo para armar el menú.
